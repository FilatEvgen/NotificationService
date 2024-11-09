package org.example.features

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.example.RedisService

fun Application.notificationFeatures() {
    val redisService = RedisService()
    val controller = NotificationsController(redisService)
    routing {
        post("/notifications") { controller.incomingNotification(call) }
    }
}