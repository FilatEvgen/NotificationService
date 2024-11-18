package org.example

import NotificationClient
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TelegramBotController(private val token: String) {
    private val bot = TelegramBot(token)

    init {
        startBot()
    }

    private fun startBot() {
        runBlocking {
            launch {
                println("Запуск обработки обновлений")
                try {
                    bot.handleUpdates()
                } catch (e: Exception) {
                    println("Ошибка при обработке обновлений: ${e.message}")

                }
            }
        }
    }

    @CommandHandler(["/start"])
    suspend fun startHandler(update: MessageUpdate?, user: User) {
        println("Обработчик start вызван для пользователя: ${user.firstName}")
        val chatId = user.id
        NotificationClient.connectToNotifications()
        message("Подключено к WebSocket и готов получать уведомления.").send(user, bot)
    }
}