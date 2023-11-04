package com.lezenford.telegram.memoirybot.telegram.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class HelpCommand : Command<SendMessage>() {
    override val command: String = "help"
    override val description: String = "Список возможных команд"
    override val publish: Boolean = true

    override suspend fun action(update: Update): SendMessage {
        return SendMessage.builder()
            .chatId(update.message.chatId)
            .text(
                """
                Бот умеет возвращать сохраненные заметки через inline режим. 
                
                /list - посмотреть ваши заметки
                /get <имя заметки> - прочитать заметку
                /add - добавить новую заметку
                /edit <имя заметки> - изменить заметку
                /delete <имя заметки> - удалить заметку 
            """.trimIndent()
            )
            .build()
    }
}