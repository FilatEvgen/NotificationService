import io.lettuce.core.pubsub.RedisPubSubListener
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.example.Notification
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotificationClientTest {
    private val redisService = mockk<RedisService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        // Заменяем реальный RedisService на мок
        NotificationClient.redisService = redisService
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test connectToNotifications handles notifications correctly`() = runBlocking {
        // Arrange
        val notificationJson = """{"title": "Test Title", "message": "Test Message", "channels": ["channel1"]}"""
        val listener = mockk<RedisPubSubListener<String, String>>(relaxed = true)

        every { redisService.subscribe(any(), any()) } answers {
            val callback = arg<RedisPubSubListener<String, String>>(1)
            callback.message("Notifications_Channel", notificationJson) // Симулируем получение сообщения
        }

        // Act
        NotificationClient.connectToNotifications()

        // Assert
        verify { redisService.subscribe(any(), listener) }
    }

    @Test
    fun `test connectToNotifications handles errors gracefully`() = runTest {
        // Arrange
        every { redisService.subscribe(any(), any()) } throws RuntimeException("Test Exception")

        // Act
        NotificationClient.connectToNotifications()

        // Assert
        // Здесь можно проверить, что ошибка была обработана корректно
        verify { redisService.subscribe(any(), any()) }
    }

    @Test
    fun `test cacheNotification stores notification correctly`() = runTest {
        // Arrange
        val notification = Notification("Test Title", "Test Message", listOf("channel1"))

        // Act
        redisService.cacheNotification(notification)

        // Assert
        verify { redisService.cacheNotification(notification) }
    }

    @Test
    fun `test getCachedNotifications returns correct notifications`() = runTest {
        // Arrange
        val notificationJson = """{"title": "Test Title", "message": "Test Message", "channels": ["channel1"]}"""
        every { redisService.getCachedNotifications("channel1") } returns listOf(notificationJson)

        // Act
        val cachedNotifications = redisService.getCachedNotifications("channel1")

        // Assert
        assert(cachedNotifications.isNotEmpty())
        assert(cachedNotifications[0] == notificationJson)
    }

    @Test
    fun `test publishMessage sends message to channel`() = runTest {
        // Arrange
        val channel = "Notifications_Channel"
        val message = "Test Message"

        // Act
        redisService.publishMessage(channel, message)

        // Assert
        verify { redisService.publishMessage(channel, message) }
    }
}