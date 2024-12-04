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
            json(Json { ignoreUnknownKeys = true })
        }
        install(WebSockets)
        defaultRequest {
            url("http://localhost:8081")
        }
    }

    private lateinit var session: WebSocketSession
    var redisService = RedisService()

    suspend fun connectToNotifications() {
        client.webSocket("/notifications/ws") {
            session = this
            WebSocketSessionManager.addSession(this)

            redisService.subscribe("Notifications_Channel", object : RedisPubSubListener<String, String> {
                override fun message(channel: String, message: String) {
                    println("Получено сообщение из канала '$channel': $message")
                    try {
                        val notification = Json.decodeFromString<Notification>(message)
                        handleNotification(notification)
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

            val cachedNotifications = redisService.getCachedNotifications("Notifications_Channel")
            cachedNotifications.forEach { notificationJson ->
                try {
                    val notification = Json.decodeFromString<Notification>(notificationJson)
                    handleNotification(notification)
                } catch (e: Exception) {
                    println("Ошибка декодирования кэшированного уведомления: ${e.message}")
                }
            }

            try {
                for (message in incoming) {
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