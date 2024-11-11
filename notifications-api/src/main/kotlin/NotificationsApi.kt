package org.example

import RedisService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import io.lettuce.core.pubsub.RedisPubSubListener
import kotlinx.coroutines.*
import org.example.features.notificationFeatures

fun main() {
    runBlocking {
        val redisService = RedisService()
        val channel = "Notifications_Channel"

        val server = embeddedServer(Netty, port = 8081, module = Application::module)
        launch(Dispatchers.IO) {
            runSubscriber(redisService, channel)
        }

        server.start(wait = true)

        Runtime.getRuntime().addShutdownHook(Thread {
            println("Закрываем соединение с Redis...")
            redisService.close()
            println("RedisService закрыт.")
        })
    }
}

suspend fun runSubscriber(redisService: RedisService, channel: String) {
    val listener = object : RedisPubSubListener<String, String> {
        override fun message(channel: String, message: String) {
            println("Получено сообщение из канала '$channel': $message")
            GlobalScope.launch {
                WebSocketSessionManager.sendToAll(message)
            }
        }
        override fun subscribed(channel: String, count: Long) {}
        override fun message(pattern: String, channel: String, message: String) {}
        override fun psubscribed(pattern: String, count: Long) {}
        override fun unsubscribed(channel: String, count: Long) {}
        override fun punsubscribed(pattern: String, count: Long) {}
    }

    try {
        redisService.subscribe(channel, listener)
    } catch (e: Exception) {
        println("Ошибка при подписке на канал: ${e.message}")
    }

    while (true) {
        delay(1000)
    }
}

fun Application.module() {
    val jsonConfig = jsonConfiguration()
    install(WebSockets)
    install(ContentNegotiation) {
        json(jsonConfig)
    }
    notificationFeatures()
}