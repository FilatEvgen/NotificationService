import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.example.Notification

class RedisService {
    private val redisClient = RedisClient.create("redis://localhost:6379")
    private val connection = redisClient.connect()
    private val asyncCommands = connection.async()
    private lateinit var pubSubConnection: StatefulRedisPubSubConnection<String, String>

    fun cacheNotification(notification: Notification) {
        for (channel in notification.channels) {
            val key = "notifications:$channel"
            val notificationJson = Json.encodeToString(notification)
            asyncCommands.lpush(key, notificationJson)
            asyncCommands.ltrim(key, 0, 99)
            println("Кэшировано уведомление в '$key': $notificationJson")
        }
    }

    fun getCachedNotifications(channel: String): List<Notification> {
        val key = "notifications:$channel"
        val notificationJsonList = asyncCommands.lrange(key, 0, -1).get() ?: emptyList() // Обработка null
        println("Кешированные уведомления из канала '$channel': $notificationJsonList")
        return notificationJsonList.map { Json.decodeFromString<Notification>(it) }
    }

    fun publishMessage(channel: String, message: String) {
        asyncCommands.publish(channel, message)
        println("Сообщение '$message' отправлено в канал '$channel'")
    }

    fun subscribe(channel: String, listener: RedisPubSubListener<String, String>) {
        pubSubConnection = redisClient.connectPubSub()
        pubSubConnection.addListener(listener)
        pubSubConnection.async().subscribe(channel)
    }

    fun close() {
        pubSubConnection.close() // Закрываем соединение pub/sub
        connection.close()
        redisClient.shutdown() // Закрываем Redis клиент
    }
}