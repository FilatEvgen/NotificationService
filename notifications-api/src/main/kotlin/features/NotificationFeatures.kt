package org.example.features

import RedisService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.example.WebSocketSessionManager
import org.example.controllers.NotificationsController

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
                            val message = frame.readText()
                            println("Получено сообщение от клиента: $message")

                            // Обработка подписки или отписки
                            when {
                                message.startsWith("subscribe:") -> {
                                    val channel = message.removePrefix("subscribe:")
                                    controller.subscribe(call, this, channel) // Передаем текущую сессию и канал
                                }

                                message.startsWith("unsubscribe:") -> {
                                    val channel = message.removePrefix("unsubscribe:")
                                    controller.unsubscribe(call, this, channel) // Передаем текущую сессию и канал
                                }
                            }
                        }

                        else -> {
                            println("Получено сообщение другого типа")
                        }
                    }
                }
            } finally {
                WebSocketSessionManager.removeSession(this)
            }
        }
    }
}