package org.example.features

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.Notification

fun Application.notificationFeatures() {
    routing {
        post ("/notifications")  {
            val notification = call.receive<Notification>()
            call.respond(notification)
        }
    }
}