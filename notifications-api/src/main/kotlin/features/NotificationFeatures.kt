package org.example.features

import RedisService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.example.WebSocketSessionManager
import org.example.controllers.NotificationsController
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Application.notificationFeatures() {
    val redisService = RedisService()
    val controller = NotificationsController(redisService)

    routing {
        post("/notifications") { controller.incomingNotification(call) }

        webSocket("/notifications/ws") {
            WebSocketSessionManager.addSession(this)

            try {
                // Обработка входящих сообщений
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val message = frame.readText()
                            println("Получено сообщение от клиента: $message")

                            // Обработка подписки или отписки
                            when {
                                message.startsWith("subscribe:") -> {
                                    val channel = message.removePrefix("subscribe:")
                                    WebSocketSessionManager.subscribeToChannel(this, channel)
                                    controller.subscribe(call, this, channel)

                                    // Отправляем кэшированные уведомления после подписки
                                    val cachedNotifications = redisService.getCachedNotifications(channel)
                                    for (notification in cachedNotifications) {
                                        try {
                                            send(notification) // Отправляем каждое кэшированное уведомление
                                        } catch (e: Exception) {
                                            println("Ошибка при отправке уведомления: ${e.message}")
                                        }
                                    }
                                }

                                message.startsWith("unsubscribe:") -> {
                                    val channel = message.removePrefix("unsubscribe:")
                                    WebSocketSessionManager.unsubscribeFromChannel(this, channel)
                                    controller.unsubscribe(call, this, channel)
                                }

                                message.startsWith("{\"type\":\"notifications_viewed\"") -> {
                                    // Обработка подтверждения просмотра уведомлений
                                    val json = Json.parseToJsonElement(message).jsonObject
                                    val viewedIds = json["ids"]?.jsonArray?.map { it.jsonPrimitive.content }

                                    if (viewedIds != null) {
                                        println("Получены идентификаторы для удаления: $viewedIds")
                                        val channels = WebSocketSessionManager.getSubscribedChannels(this)
                                        if (channels != null) {
                                            for (channel in channels) {
                                                redisService.removeNotifications(channel, viewedIds)
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    println("Получено сообщение другого типа")
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