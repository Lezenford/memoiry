package com.lezenford.telegram.memoirybot.telegram.command

import com.lezenford.telegram.memoirybot.service.HintService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class DeleteCommand(
    private val hintService: HintService
) : Command<SendMessage>() {
    override val command: String = "delete"
    override val description: String = "Удалить заметку"
    override val publish: Boolean = true

    override suspend fun action(update: Update): SendMessage? {
        val builder = SendMessage.builder().chatId(update.message.chatId)
        val key = update.message?.text?.split(" ")?.drop(1)?.firstOrNull()
            ?: return builder.text("Вы забыли ввести название заметки, попробуйте еще раз").build()

        val hint = hintService.find(update.message.from.id, key)
            ?: return builder.text("Заметки $key не существует. Проверьте список существующих заметок /list").build()
        hintService.delete(hint)
        return builder.text(
            """
            Заметка $key успешно удалена. Вот что в ней было написано: 
            
            ${hint.value}
            
            Если хотите снова ее добавить - воспользуйтесь /add
            """.trimIndent()
        ).build()
    }
}