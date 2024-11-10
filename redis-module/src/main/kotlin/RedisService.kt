import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubListener

class RedisService {
    private val redisClient = RedisClient.create("redis://localhost:6379") // Убедитесь, что адрес правильный
    private val connection = redisClient.connect()
    private val asyncCommands = connection.async()

    fun publishMessage(channel: String, message: String) {
        asyncCommands.publish(channel, message)
        println("Сообщение '$message' отправлено в канал '$channel'")
    }

    fun subscribe(channel: String, listener: RedisPubSubListener<String, String>) {
        val pubSubConnection = redisClient.connectPubSub()
        pubSubConnection.addListener(listener)
        pubSubConnection.async().subscribe(channel)
    }

    fun close() {
        connection.close()
        redisClient.shutdown()
    }
}