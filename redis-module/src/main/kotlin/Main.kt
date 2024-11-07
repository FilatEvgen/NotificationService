package org.example

fun main() {
    val redisService = RedisService()
    val key = "testKey"
    val message = "testMessage"
    redisService.sendMessage(key, message)
    println("Сообщение отправлено: $message")

    val returnMessage = redisService.getMessage(key)
    println("Сообщение возвращено: $returnMessage")

    redisService.close()
}