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


object NotificationClient {
    private val client = HttpClient(CIO){
        install(ContentNegotiation) {
            json(Json{
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
            for (message in incoming) {
                when (message) {
                    is Frame.Text -> {
                        val notification = Json.decodeFromString<Notification>(message.readText())
                        println("Получено уведомление: ${notification.title} - ${notification.message}")
                    }
                    else -> {
                        println("Получен другой тип сообщения")
                    }
                }
            }
        }
    }
    fun cloe()  {
        client.close()
    }
}
fun main () = runBlocking{
    try {
        NotificationClient.connectToNotifications()
    }catch (e: Exception) {
        println("Ошибка: ${e.message}")
    }finally {
        NotificationClient.cloe()
    }
}