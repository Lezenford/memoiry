package com.lezenford.telegram.memoirybot.telegram.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class StartCommand : Command<SendMessage>() {
    override val command: String = "start"

    override suspend fun action(update: Update): SendMessage {
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text("Привет! Ты можешь загружать в меня свои заметки. Подробности в /help")
            .build()
    }
}