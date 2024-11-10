package org.example

import RedisService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.*
import io.lettuce.core.pubsub.RedisPubSubListener
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
        }
        override fun subscribed(channel: String, count: Long) {
            println("Подписались на канал '$channel'. Количество подписчиков: $count")
        }
        override fun message(pattern: String, channel: String, message: String) {
            println("Получено сообщение по паттерну '$pattern' в канале '$channel': $message")
        }
        override fun psubscribed(pattern: String, count: Long) {
            println("Подписались на паттерн '$pattern'. Количество подписчиков: $count")
        }
        override fun unsubscribed(channel: String, count: Long) {
            println("Отписались от канала '$channel'. Количество подписчиков: $count")
        }
        override fun punsubscribed(pattern: String, count: Long) {
            println("Отписались от паттерна '$pattern'. Количество подписчиков: $count")
        }
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
    install(ContentNegotiation) {
        json(jsonConfig)
    }
    notificationFeatures()
}