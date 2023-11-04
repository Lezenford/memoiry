package com.lezenford.telegram.memoirybot.telegram.command

import com.lezenford.telegram.memoirybot.service.HintService
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ListCommand(
    private val hintService: HintService
) : Command<SendMessage>() {
    override val command: String = "list"
    override val description: String = "Список заметок"

    override suspend fun action(update: Update): SendMessage? {
        return hintService.list(userId = update.message.from.id).map { it.key }.let { keys ->
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    """
                    Ваши заметки:
                    ${keys.fold(StringBuffer()) { acc, key -> acc.append("\n").append(key) }}
                    """.trimIndent()
                ).build()
        }
    }
}