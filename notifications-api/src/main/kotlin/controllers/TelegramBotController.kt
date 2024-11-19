package org.example.controllers

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import org.example.Commands.START

object ChatStorage {
    var chatId: Long? = null
}

class TelegramBotController(private val telegramBot: TelegramBot) {

    @CommandHandler([START])
    suspend fun start(user: User) {
        println("Обработчик start вызван для пользователя: ${user.firstName}")


        ChatStorage.chatId = user.id

        NotificationClient.connectToNotifications()

        // Отправляем сообщение пользователю
        message { "Подключено к WebSocket и готов получать уведомления." }.send(user, telegramBot)
    }
}