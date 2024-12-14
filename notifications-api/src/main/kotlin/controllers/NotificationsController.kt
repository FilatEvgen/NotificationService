package org.example.controllers
import RedisService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.NotificationCreateRequest
import org.example.WebSocketSessionManager
import java.util.UUID
import org.example.Notification

class NotificationsController(private val redisService: RedisService) {

    suspend fun incomingNotification(call: ApplicationCall) {
        try {
            // Получаем запрос на создание уведомления
            val notificationRequest = call.receive<NotificationCreateRequest>()

            // Создаем уведомление со сгенерированным id
            val notification = Notification(
                id = UUID.randomUUID().toString(),
                title = notificationRequest.title,
                message = notificationRequest.message,
                channels = notificationRequest.channels
            )
            println("Получено уведомление: ${notification.title} - ${notification.message} с id: ${notification.id}")

            // Кешируем уведомление
            redisService.cacheNotification(notification)

            // Отправляем сообщение в каждый канал из списка
            for (channel in notification.channels) {
                redisService.publishMessage(channel, Json.encodeToString(notification))
                println("Сообщение отправлено в Redis: ${notification.message} в канал: $channel")
            }

            call.respond(HttpStatusCode.OK, "Сообщение отправлено: ${notification.message}")
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun subscribe(call: ApplicationCall, session: DefaultWebSocketSession, channel: String) {
        WebSocketSessionManager.subscribeToChannel(session, channel)
        call.respond(HttpStatusCode.OK, "Подписано на канал: $channel")
    }

    suspend fun unsubscribe(call: ApplicationCall, session: DefaultWebSocketSession, channel: String) {
        WebSocketSessionManager.unsubscribeFromChannel(session, channel)
        call.respond(HttpStatusCode.OK, "Отписался от канала: $channel")
    }
}

private suspend fun handleError(call: ApplicationCall, e: Exception) {
    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Произошла неизвестная ошибка")
}