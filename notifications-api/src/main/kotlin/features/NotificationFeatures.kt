package org.example.features

import org.example.controllers.NotificationsController
import RedisService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.example.WebSocketSessionManager

fun Application.notificationFeatures() {
    val redisService = RedisService()
    val controller = NotificationsController(redisService)

    routing {
        post("/notifications") { controller.incomingNotification(call) }

        webSocket("/notifications/ws") {
            WebSocketSessionManager.addSession(this)

            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            println("Получено сообщение от клиента: ${frame.readText()}")
                        }
                        else -> {
                            println("Получен другой тип сообщения")
                        }
                    }
                }
            } finally {
                WebSocketSessionManager.removeSession(this)
            }
        }
    }
}