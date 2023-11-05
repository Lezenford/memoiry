package com.lezenford.telegram.memoirybot.telegram.command

import com.lezenford.telegram.memoirybot.service.HintService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class GetCommand(
    private val hintService: HintService
) : Command<SendMessage>() {
    override val command: String = "get"
    override val description: String = "Посмотреть данные о заметке"
    override val publish: Boolean = true

    override suspend fun action(update: Update): SendMessage? {
        val builder = SendMessage.builder().chatId(update.message.chatId)
        val key = update.message?.text?.replaceFirst("$COMMAND_INIT_CHARACTER$command ", "")
            ?: return builder.text("Вы забыли ввести название заметки, попробуйте еще раз").build()

        val hint = hintService.find(update.message.from.id, key)
            ?: return builder.text("Заметка $key не найдена. Если хотите ее создать - воспользуйтесь /add").build()
        return builder.text("${hint.key} - ${hint.value}").build()
    }
}