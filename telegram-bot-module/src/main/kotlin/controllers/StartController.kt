package org.example.controllers

import NotificationClient
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import org.example.Commands.START

class StartController {
    @CommandHandler([START])
    suspend fun start(user: User, bot: TelegramBot) {
        // Запускаем WebSocket соединение
        try {
            NotificationClient.connectToNotifications()
            message("Вы успешно подключены к WebSocket. Ожидайте уведомления!")
                .send(user, bot) // Отправка сообщения пользователю
        } catch (e: Exception) {
            message("Ошибка при подключении к WebSocket: ${e.message}")
                .send(user, bot) // Отправка сообщения об ошибке
        }
    }
}