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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
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
    val listener = object : RedisPubSubListener<String, String> {
        override fun message(channel: String, message: String) {
            println("Получено сообщение из канала '$channel': $message")
            // Обработка сообщения
            try {
                val notification = Json.decodeFromString<Notification>(message)
            } catch (e: SerializationException) {
                println("Ошибка десериализации сообщения: ${e.message}. Сообщение: $message")
            } catch (e: Exception) {
                println("Ошибка при обработке сообщения: ${e.message}")
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
        redisService.subscribe(channel, listener)
    } catch (e: Exception) {
        println("Ошибка при подписке на канал: ${e.message}")
    }

    // Бесконечный цикл для поддержания подписки
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
    notificationFeatures()
}