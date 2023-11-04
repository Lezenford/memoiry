package com.lezenford.telegram.memoirybot.telegram.command

import com.lezenford.telegram.memoirybot.service.HintService
import com.lezenford.telegram.memoirybot.telegram.handler.ScriptHandler
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class EditCommand(
    private val hintService: HintService,
    private val scriptHandler: ScriptHandler
) : Command<SendMessage>() {
    override val command: String = "edit"
    override val description: String = "Изменить подсказку"
    override val publish: Boolean = true

    override suspend fun action(update: Update): SendMessage? {
        val chatId = update.message.chatId
        val builder = SendMessage.builder().chatId(chatId)
        val key = update.message?.text?.split(" ")?.drop(1)?.firstOrNull()
            ?: return builder.text("Вы забыли ввести название заметки, попробуйте еще раз").build()
        var hint = hintService.find(update.message.from.id, key)
            ?: return builder.text("Заметка $key не найдена. Проверьте список существующих заметок /list").build()
        return builder.text(
            """
            В заметке $key лежит следующее:
            
            ${hint.value}
            
            На что вы хотите заменить содержимое?
            """.trimIndent()
        ).build().apply {
            scriptHandler.addScript(chatId) { message ->
                return@addScript when {
                    message.hasPhoto() || message.hasAudio() || message.hasAnimation() || message.hasContact() || message.hasDice() || message.hasDocument() || message.hasLocation() || message.hasInvoice() || message.hasPassportData() || message.hasPoll() ->
                        builder.text("К сожалению, это нельзя сохранить в заметки. Попробуйте снова /editd")
                            .build()

                    message.hasText() -> {
                        hint = hintService.find(update.message.from.id, key)
                            ?: return@addScript builder.text("Заметка $key не найдена. Проверьте список существующих заметок /list")
                                .build()
                        hint.value = message.text
                        hintService.save(hint)
                        builder.text("Заметка изменена. Вы можете вызвать ее через inline режим, либо командой /get")
                            .build()
                    }

                    else -> builder.text("К сожалению, это нельзя сохранить в заметки. Попробуйте снова /add")
                        .build()
                }
            }
        }
    }
}