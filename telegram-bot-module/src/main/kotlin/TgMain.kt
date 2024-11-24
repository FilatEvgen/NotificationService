package org.example

import eu.vendeli.tgbot.TelegramBot

suspend fun main() {
    val bot = TelegramBot(System.getenv("TG_TOKEN"))
    bot.handleUpdates()
}