package org.example.features

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.Notification

class NotificationsController {
    suspend fun incomingNotification(call: ApplicationCall) {
        try {
            val notification = call.receive<Notification>()
            call.respond(notification)
        } catch (e: Exception) {
            handleError(call, e)
        }
    }
    private suspend fun handleError(call: ApplicationCall, e: Exception) {
        call.respond(HttpStatusCode.InternalServerError, e.message?: "Произошла неизвестная ошибка")
    }
}