import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.Notification
import org.example.WebSocketSessionManager

object NotificationClient {
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
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

    suspend fun connectToNotifications() {
        client.ws("/notifications/ws") {
            try {
                WebSocketSessionManager.addSession(this)
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
    private suspend fun sendNotificationToBot(notification: Notification) {
        println("Отправлено уведомление в Telegram: ${notification.title} - ${notification.message}")
    }

    fun close() {
        client.close()
    }
}

fun main() = runBlocking {
    try {
        NotificationClient.connectToNotifications()
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
    } finally {
        NotificationClient.close()
    }
}