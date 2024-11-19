package org.example.controllers

import RedisService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.Notification

class NotificationsController(private val redisService: RedisService) {
    suspend fun incomingNotification(call: ApplicationCall) {
        try {
            val notification = call.receive<Notification>()
            println("Получено уведомление: ${notification.title} - ${notification.message}")

            redisService.publishMessage("Notifications_Channel", Json.encodeToString(notification))
            println("Сообщение отправлено в Redis: ${notification.message}")
            call.respond(HttpStatusCode.OK, "Сообщение отправлено: ${notification.message}")
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    private suspend fun handleError(call: ApplicationCall, e: Exception) {
        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Произошла неизвестная ошибка")
    }
}