package org.example

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection

class RedisService {
    private val redisClient: RedisClient = RedisClient.create("redis://localhost:6379")
    private val connection: StatefulRedisConnection <String, String> = redisClient.connect()
    private val syncCommands: RedisCommands <String, String> = connection.sync()
    private val pubSubConnection: StatefulRedisPubSubConnection <String, String> = redisClient.connectPubSub()

    fun sendMessage(key: String, message: String) {
        println("Отправляем сообщение '$message' в очередь '$key'")
        syncCommands.rpush(key, message)
    }
    fun getMessage(key: String): String? {
        return syncCommands.rpop(key)
    }
    fun publishMessage(channel: String, message: String) {
        pubSubConnection.sync().publish(channel, message)
    }
    fun subscribe(channel: String, listener: RedisPubSubListener<String, String>)   {
        pubSubConnection.addListener(listener)
        pubSubConnection.sync().subscribe(channel)
    }
    fun close(){
        connection.close()
        pubSubConnection.close()
        redisClient.shutdown()
    }
}