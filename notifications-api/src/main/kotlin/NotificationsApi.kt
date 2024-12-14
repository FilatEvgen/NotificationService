package org.example

import RedisService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.lettuce.core.pubsub.RedisPubSubListener
import kotlinx.coroutines.*
import org.example.features.notificationFeatures

fun main() {
    runBlocking {
        val redisService = RedisService()
        val channel = "Notifications_Channel"
        val serverJob = launch(Dispatchers.IO) {
            embeddedServer(Netty, port = 8081, module = Application::module).start(wait = true)
        }
        delay(1000)
        launch(Dispatchers.IO) {
            runSubscriber(redisService, channel)
        }
        serverJob.join()
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Закрываем соединение с Redis...")
            redisService.close()
            println("RedisService закрыт.")
        })
    }
}

suspend fun runSubscriber(redisService: RedisService, channel: String) = coroutineScope {
    val channels = listOf("Notifications_Channel", "Another_Channel")
    val listener = object : RedisPubSubListener<String, String> {
        override fun message(channel: String, message: String) {
            println("Получено сообщение из канала '$channel': $message")
            launch {
                WebSocketSessionManager.sendMessageToChannel(channel, message)
            }
        }

        override fun subscribed(channel: String, count: Long) {
            println("Подписано на канал '$channel'. Текущий счетчик: $count")
        }

        override fun message(pattern: String, channel: String, message: String) {
            println("Получено сообщение по паттерну '$pattern' из канала '$channel': $message")
        }

        override fun psubscribed(pattern: String, count: Long) {
            println("Подписано на паттерн '$pattern'. Текущий счетчик: $count")
        }

        override fun unsubscribed(channel: String, count: Long) {
            println("Отписано от канала '$channel'. Текущий счетчик: $count")
        }

        override fun punsubscribed(pattern: String, count: Long) {
            println("Отписано от паттерна '$pattern'. Текущий счетчик: $count")
        }
    }

    try {
        channels.forEach { channel ->
            redisService.subscribe(channel, listener)
        }
    } catch (e: Exception) {
        println("Ошибка при подписке на канал: ${e.message}")
    }

    while (true) {
        delay(3000)
    }
}

fun Application.module() {
    val jsonConfig = jsonConfiguration()
    install(WebSockets)
    install(ContentNegotiation) {
        json(jsonConfig)
    }

    // Настройка маршрутов для статических файлов
    routing {
        staticResources("/static", "static") // Обслуживание статических файлов из папки resources/static

        get("/") {
            call.respondRedirect("/static/index.html") // Перенаправление на главную страницу
        }

        notificationFeatures() // Вызов функции для настройки уведомлений
    }
}

fun handleDeleteNotifications(viewedIds: List<String>, channel: String) {
    val redisService = RedisService()
    redisService.removeNotifications(channel, viewedIds)

    // Получаем обновленный список уведомлений
    val updatedNotifications = redisService.getCachedNotifications(channel)
    // Отправляем обновленный список на клиент
    sendUpdatedNotificationsToClient(updatedNotifications, channel)
}

fun sendUpdatedNotificationsToClient(notifications: List<String>, channel: String) {
    notifications.forEach { notification ->
        // Отправляем каждое уведомление на соответствующий канал
        runBlocking {
            WebSocketSessionManager.sendMessageToChannel(channel, notification)
        }
    }
}