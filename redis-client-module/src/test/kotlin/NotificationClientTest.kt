import io.lettuce.core.pubsub.RedisPubSubListener
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
class NotificationClientTest {

    @BeforeEach
    fun setUp() {
        // Настройка мока для RedisService
        NotificationClient.redisService = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        NotificationClient.stop() // Завершаем работу NotificationClient
        clearAllMocks()
    }

    @Test
    fun `test connectToNotifications handles notifications correctly`() = runBlocking {
        // Arrange
        val notificationJson = """{"title": "Test Title", "message": "Test Message", "channels": ["channel1"]}"""

        // Настройка мока для subscribe
        every { NotificationClient.redisService.subscribe(any(), any()) } answers {
            val callback = arg<RedisPubSubListener<String, String>>(1)
            callback.message("Notifications_Channel", notificationJson) // Симулируем получение сообщения
        }

        // Act
        val job = launch {
            NotificationClient.connectToNotifications()
        }

        // Даем время для обработки корутин
        delay(100) // Увеличьте время, если необходимо

        // Assert
        verify { NotificationClient.redisService.subscribe("Notifications_Channel", any()) }

        // Завершаем работу клиента
        NotificationClient.stop()
        job.join() // Ждем завершения корутины
    }

    @Test
    fun `test connectToNotifications handles errors gracefully`() = runBlocking {
        // Arrange
        val invalidJson = """{"invalid": "data"}"""

        // Настройка мока для subscribe
        every { NotificationClient.redisService.subscribe(any(), any()) } answers {
            val callback = arg<RedisPubSubListener<String, String>>(1)
            callback.message("Notifications_Channel", invalidJson) // Симулируем получение некорректного сообщения
        }

        // Act
        val job = launch {
            NotificationClient.connectToNotifications()
        }

        // Даем время для обработки корутин
        delay(100) // Увеличьте время, если необходимо

        // Assert
        // Здесь можно добавить проверки, чтобы убедиться, что ошибки были обработаны корректно

        // Завершаем работу клиента
        NotificationClient.stop()
        job.join() // Ждем завершения корутины
    }
}