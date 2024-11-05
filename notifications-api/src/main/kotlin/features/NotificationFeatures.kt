package org.example.features

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.notificationFeatures() {
    val controller = NotificationsController()
    routing {
        post("/notifications") { controller.incomingNotification(call) }
    }
}