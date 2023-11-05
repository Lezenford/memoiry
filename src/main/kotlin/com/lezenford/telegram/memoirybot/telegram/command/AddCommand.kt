package com.lezenford.telegram.memoirybot.telegram.command

import com.lezenford.telegram.memoirybot.model.entity.Hint
import com.lezenford.telegram.memoirybot.service.HintService
import com.lezenford.telegram.memoirybot.telegram.handler.ScriptHandler
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class AddCommand(
    private val hintService: HintService,
    private val scriptHandler: ScriptHandler
) : Command<SendMessage>() {
    override val command: String = "add"
    override val description: String = "Добавить новую заметку"
    override val publish: Boolean = true

    override suspend fun action(update: Update): SendMessage? {
        val chatId = update.message.chatId
        val builder = SendMessage.builder().chatId(chatId)
        return builder.text("Введите название для вашей заметки")
            .build().apply {
                scriptHandler.addScript(chatId) key@{ message ->
                    if (message.text.length > 255) {
                        return@key builder.text("Слишком длинное название, попробуйте еще раз /add").build()
                    }
                    val key = message.text
                    hintService.find(message.from.id, key)?.also {
                        return@key builder.text("Такая заметка уже существует, вы можете ее посмотреть /get")
                            .build()
                    }
                    return@key builder.text("Что положить в заметку?").build().apply {
                        scriptHandler.addScript(chatId) value@{ message ->
                            return@value when {
                                message.hasPhoto() || message.hasAudio() || message.hasAnimation() || message.hasContact() || message.hasDice() || message.hasDocument() || message.hasLocation() || message.hasInvoice() || message.hasPassportData() || message.hasPoll() ->
                                    builder.text("К сожалению, это нельзя сохранить в заметки. Попробуйте снова /add")
                                        .build()

                                message.hasText() -> {
                                    hintService.save(
                                        Hint(
                                            userId = message.from.id,
                                            key = key,
                                            value = message.text,
                                            type = Hint.Type.TEXT
                                        )
                                    )
                                    builder.text("Заметка сохранена. Вы можете вызвать ее через inline режим, либо командой /get")
                                        .build()
                                }

                                else -> builder.text("К сожалению, это нельзя сохранить в заметки. Попробуйте снова /add")
                                    .build()
                            }
                        }
                    }
                }
            }
    }
}