import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import io.lettuce.core.pubsub.RedisPubSubListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.Notification
import org.example.WebSocketSessionManager

object NotificationClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(WebSockets)
        defaultRequest {
            url("http://localhost:8081")
        }
    }

    private lateinit var session: WebSocketSession
    private val redisService = RedisService()

    suspend fun connectToNotifications() {
        client.webSocket("/notifications/ws") {
            session = this // Сохраняем текущую сессию
            WebSocketSessionManager.addSession(this)

            // Создаем слушателя для обработки сообщений из Redis
            val listener = object : RedisPubSubListener<String, String> {
                override fun message(channel: String, message: String) {
                    println("Получено сообщение из канала '$channel': $message")
                    // Запускаем корутину для обработки уведомления
                    runBlocking {
                        try {
                            val notification = Json.decodeFromString<Notification>(message)
                            sendNotificationToBot(notification)
                        } catch (e: Exception) {
                            println("Ошибка при десериализации уведомления: ${e.message}")
                        }
                    }
                }

                override fun message(pattern: String, channel: String, message: String) {
                    println("Получено сообщение из паттерна '$pattern' и канала '$channel': $message")
                    // Обработка сообщения из паттерна и канала
                }

                override fun subscribed(channel: String, count: Long) {
                    println("Подписан на канал '$channel', общее количество подписок: $count")
                }

                override fun unsubscribed(channel: String, count: Long) {
                    println("Отписан от канала '$channel', общее количество подписок: $count")
                }

                override fun psubscribed(pattern: String, count: Long) {
                    println("Подписан на паттерн '$pattern', общее количество подписок: $count")
                }

                override fun punsubscribed(pattern: String, count: Long) {
                    println("Отписан от паттерна '$pattern', общее количество подписок: $count")
                }
            }

            // Подписываемся на два канала
            redisService.subscribe("Notifications_Channel", listener)
            redisService.subscribe("Another_Channel", listener)

            // Получаем кэшированные уведомления из обоих каналов
            val cachedNotifications = listOf(
                redisService.getCachedNotifications("Notifications_Channel"),
                redisService.getCachedNotifications("Another_Channel")
            )
            cachedNotifications.flatten().forEach { notification ->
                sendNotificationToBot(notification)
            }

            try {
                for (message in incoming) {
                    when (message) {
                        is Frame.Text -> {
                            val notificationsMessage = Json.decodeFromString<Notification>(message.readText())
                            sendNotificationToBot(notificationsMessage)
                        }
                        else -> {
                            println("Получен другой тип сообщения")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Ошибка при получении сообщения: ${e.message}")
            } finally {
                WebSocketSessionManager.removeSession(this)
            }
        }
    }

    private suspend fun sendNotificationToBot(notification: Notification) {
        println("Отправлено уведомление в Telegram: ${notification.title} - ${notification.message}")
    }

    fun close() {
        client.close()
    }
}

fun main() = runBlocking {
    try {
        // Подключаемся к уведомлениям
        NotificationClient.connectToNotifications()
        // Задержка для демонстрации
        delay(10000) // Ждем 10 секунд для получения сообщений
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
    } finally {
        NotificationClient.close()
    }
}