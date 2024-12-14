import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import io.lettuce.core.pubsub.RedisPubSubListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        install(Logging) {
            level = LogLevel.ALL
        }
        defaultRequest {
            url("http://localhost:8081")
        }
        engine{
            requestTimeout = 60_000
        }
    }

    private lateinit var session: WebSocketSession
    var redisService = RedisService()
    var isRunning = false

    suspend fun connectToNotifications() {
        isRunning = true
        try {
            println("Попытка подключения к WebSocket.......")
            client.webSocket("/notifications/ws") {
                session = this
                WebSocketSessionManager.addSession(this)
                println("Подключение к WebSocket успешно....")

                // Подписка на Redis
                redisService.subscribe("Notifications_Channel", object : RedisPubSubListener<String, String> {
                    override fun message(channel: String, message: String) {
                        println("Получено сообщение из канала '$channel': $message")
                        try {
                            val notification = Json.decodeFromString<Notification>(message)
                            handleNotification(notification)
                            launch {
                                println("Отправка уведомления через WebSocket: $message")
                                session.send(message)
                            }
                        } catch (e: Exception) {
                            println("Ошибка декодирования сообщения: ${e.message}")
                        }
                    }

                    override fun message(pattern: String, channel: String, message: String) {}
                    override fun subscribed(channel: String, count: Long) {}
                    override fun unsubscribed(channel: String, count: Long) {}
                    override fun psubscribed(pattern: String, count: Long) {}
                    override fun punsubscribed(pattern: String, count: Long) {}
                })

                // Получение кэшированных уведомлений
                val cachedNotifications = redisService.getCachedNotifications("Notifications_Channel")
                cachedNotifications.forEach { notificationJson ->
                    try {
                        val notification = Json.decodeFromString<Notification>(notificationJson)
                        handleNotification(notification)
                        launch {
                            println("Отправка кэшированного уведомления через WebSocket: $notificationJson")
                            session.send(notificationJson)
                        }
                    } catch (e: Exception) {
                        println("Ошибка декодирования кэшированного уведомления: ${e.message}")
                    }
                }

                // Обработка входящих сообщений
                for (message in incoming) {
                    if (!isRunning) break // Проверка на завершение
                    when (message) {
                        is Frame.Text -> {
                            val notificationsMessage = Json.decodeFromString<Notification>(message.readText())
                            handleNotification(notificationsMessage)
                        }
                        else -> {
                            println("Получен другой тип сообщения")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Ошибка при подключении к WebSocket: ${e.message}")
        } finally {
            isRunning = false
            WebSocketSessionManager.removeSession(session as DefaultWebSocketSession)
        }
    }

    fun stop() {
        isRunning = false // Устанавливаем флаг завершения
        // Здесь можно добавить логику для отключения от WebSocket, если это необходимо
    }

    private fun handleNotification(notification: Notification) {
        println("Обработка уведомления: Заголовок: ${notification.title}, Сообщение: ${notification.message}, Каналы: ${notification.channels.joinToString(", ")}")
    }

}

fun main() = runBlocking {
    launch {
        try {
            NotificationClient.connectToNotifications()
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
        }
    }
    while (true) {
        delay(10000)
    }
}