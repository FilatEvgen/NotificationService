import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
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
            json(Json { ignoreUnknownKeys = true }) // Настройка JSON-сериализации
        }
        install(WebSockets) // Установка поддержки WebSocket
        defaultRequest {
            url("http://localhost:8081") // Базовый URL для запросов
        }
    }

    private lateinit var session: WebSocketSession
    private val redisService = RedisService() // Предполагается, что это ваш сервис для работы с Redis

    suspend fun connectToNotifications() {
        client.webSocket("/notifications/ws") {
            session = this // Сохраняем текущую сессию
            WebSocketSessionManager.addSession(this)

            // Подписываемся на каналы Redis
            redisService.subscribe("Notifications_Channel", object : RedisPubSubListener<String, String> {
                override fun message(channel: String, message: String) {
                    println("Получено сообщение из канала '$channel': $message")
                    // Обработка уведомления
                    val notification = Json.decodeFromString<Notification>(message) // Декодируем сообщение в Notification
                    handleNotification(notification) // Передаем объект Notification
                }

                // Остальные методы оставляем пустыми или добавляем логи
                override fun message(pattern: String, channel: String, message: String) {}
                override fun subscribed(channel: String, count: Long) {}
                override fun unsubscribed(channel: String, count: Long) {}
                override fun psubscribed(pattern: String, count: Long) {}
                override fun punsubscribed(pattern: String, count: Long) {}
            })

            // Получаем кэшированные уведомления из канала
            val cachedNotifications = redisService.getCachedNotifications("Notifications_Channel")
            cachedNotifications.forEach { notificationJson ->
                val notification = Json.decodeFromString<Notification>(notificationJson.toString()) // Декодируем JSON в Notification
                handleNotification(notification) // Передаем объект Notification
            }

            try {
                for (message in incoming) {
                    when (message) {
                        is Frame.Text -> {
                            val notificationsMessage = Json.decodeFromString<Notification>(message.readText()) // Декодируем текст сообщения в Notification
                            handleNotification(notificationsMessage) // Передаем объект Notification
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

    private fun handleNotification(notification: Notification) {
        println("Обработка уведомления: Заголовок: ${notification.title}, Сообщение: ${notification.message}, Каналы: ${notification.channels.joinToString(", ")}")
    }

    fun close() {
        client.close() // Закрываем клиент
    }
}

fun main() = runBlocking {
    launch {
        try {
            NotificationClient.connectToNotifications() // Подключаемся к уведомлениям
        } catch (e: Exception) {
            println("Ошибка: ${e.message}") // Обработка ошибок
        }
    }
    delay(10000) // Ждем 10 секунд для получения сообщений
    NotificationClient.close() // Закрываем клиент после ожидания
}