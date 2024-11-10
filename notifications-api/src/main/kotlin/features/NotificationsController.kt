import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.Notification

class NotificationsController(private val redisService: RedisService) {
    suspend fun incomingNotification(call: ApplicationCall) {
        try {
            val notification = call.receive<Notification>()
            redisService.publishMessage("Notifications_Channel", notification.message)
            println("Сообщение отправлено: ${notification.message}")
            call.respond(HttpStatusCode.OK, "Сообщение отправлено: ${notification.message}")
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    private suspend fun handleError(call: ApplicationCall, e: Exception) {
        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Произошла неизвестная ошибка")
    }
}