package org.example.features

import NotificationsController
import RedisService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.notificationFeatures() {
    val redisService = RedisService()
    val controller = NotificationsController(redisService)
    routing {
        post("/notifications") { controller.incomingNotification(call) }
    }
}