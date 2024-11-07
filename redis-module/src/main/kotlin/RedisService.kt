package org.example

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisService {
    private val redisClient: RedisClient = RedisClient.create("redis://localhost:6379")
    private val connection: StatefulRedisConnection <String, String> = redisClient.connect()
    private val syncCommands: RedisCommands <String, String> = connection.sync()

    fun sendMessage(key: String, message: String) {
        syncCommands.set(key, message)
    }
    fun getMessage(key: String): String? {
        return syncCommands.get(key)
    }
    fun close(){
        connection.close()
        redisClient.shutdown()
    }
}