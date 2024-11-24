import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.Notification
import org.example.WebSocketSessionManager

object NotificationClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(WebSockets)
        defaultRequest {
            url("http://localhost:8081")
        }
    }

    private lateinit var session: WebSocketSession

    suspend fun connectToNotifications() {
        client.webSocket("/notifications/ws") {
            session = this // Сохраняем текущую сессию
            WebSocketSessionManager.addSession(this)

            try {
                for (message in incoming) {
                    when (message) {
                        is Frame.Text -> {
                            val notification = Json.decodeFromString<Notification>(message.readText())
                            sendNotificationToBot(notification)
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

    suspend fun subscribeToChannel(channel: String) {
        session.send(Frame.Text("Подписались:$channel")) // Отправляем команду подписки
    }

    suspend fun unsubscribeFromChannel(channel: String) {
        session.send(Frame.Text("Отписались:$channel")) // Отправляем команду отписки
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
        // Пример подписки на канал
        NotificationClient.subscribeToChannel("Notifications_Channel")
        println("Subscribed to Notifications_Channel")
        // Задержка для демонстрации
        delay(10000) // Ждем 10 секунд для получения сообщений
        // Пример отписки от канала
        NotificationClient.unsubscribeFromChannel("Notifications_Channel")
        println("Отписались от Notifications_Channel")
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
    } finally {
        NotificationClient.close()
    }
}