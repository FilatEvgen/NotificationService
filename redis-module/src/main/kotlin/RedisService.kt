import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.RedisPubSubListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.Notification


class RedisService {
    private val redisClient = RedisClient.create("redis://localhost:6379")
    private val connection = redisClient.connect()
    private val asyncCommands = connection.async()
    private lateinit var pubSubConnection: StatefulRedisPubSubConnection<String, String>

    fun cacheNotification(notification: Notification) {
        try {
            for (channel in notification.channels) {
                val key = "notifications:$channel"
                val notificationJson = Json.encodeToString(notification)
                asyncCommands.lpush(key, notificationJson)
                asyncCommands.ltrim(key, 0, 99)
                println("Кэшировано уведомление в '$key': $notificationJson")
            }
        } catch (e: Exception) {
            println("Ошибка при кэшировании уведомления: ${e.message}")
        }
    }

    fun getCachedNotifications(channel: String): List<String> {
        val key = "notifications:$channel"
        return try {
            val notificationJsonList = asyncCommands.lrange(key, 0, -1).get() ?: emptyList()
            println("Кешированные уведомления из канала '$channel': $notificationJsonList")
            notificationJsonList
        } catch (e: Exception) {
            println("Ошибка при получении кэшированных уведомлений: ${e.message}")
            emptyList()
        }
    }

    fun publishMessage(channel: String, message: String) {
        try {
            asyncCommands.publish(channel, message)
            println("Сообщение '$message' отправлено в канал '$channel'")
        } catch (e: Exception) {
            println("Ошибка при отправке сообщения в канал '$channel': ${e.message}")
        }
    }

    fun subscribe(channel: String, listener: RedisPubSubListener<String, String>) {
        try {
            pubSubConnection = redisClient.connectPubSub()
            pubSubConnection.addListener(listener)
            pubSubConnection.async().subscribe(channel)
            println("Подписка на канал '$channel' выполнена.")
        } catch (e: Exception) {
            println("Ошибка при подписке на канал '$channel': ${e.message}")
        }
    }

    fun removeNotifications(channel: String, viewedIds: List<String>) {
        val key = "notifications:$channel"
        try {
            for (id in viewedIds) {
                // Получаем все уведомления из канала
                val notifications = asyncCommands.lrange(key, 0, -1).get() ?: emptyList()

                // Ищем уведомление с данным id
                val notificationToRemove = notifications.find { notificationJson ->
                    val notification = Json.decodeFromString<Notification>(notificationJson)
                    notification.id == id
                }

                if (notificationToRemove != null) {
                    // Удаляем уведомление по его JSON-строке
                    val removedCount = asyncCommands.lrem(key, 0, notificationToRemove).get()
                    if (removedCount > 0) {
                        println("Удалено уведомление с id '$id' из канала '$channel'")
                    } else {
                        println("Уведомление с id '$id' не найдено в канале '$channel'")
                    }
                } else {
                    println("Уведомление с id '$id' не найдено в канале '$channel'")
                }
            }
        } catch (e: Exception) {
            println("Ошибка при удалении уведомлений: ${e.message}")
        }
    }

    fun close() {
        try {
            if (::pubSubConnection.isInitialized) {
                pubSubConnection.close() // Закрываем соединение pub/sub
            }
            connection.close()
        } catch (e: Exception) {
            println("Ошибка при закрытии соединений: ${e.message}")
        } finally {
            redisClient.shutdown() // Закрываем Redis клиент
        }
    }
}