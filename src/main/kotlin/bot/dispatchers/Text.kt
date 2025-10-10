package com.lezenford.telegram.memoiry.bot.dispatchers

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.lezenford.telegram.memoiry.model.entity.Hint
import com.lezenford.telegram.memoiry.model.repository.HintRepository
import com.lezenford.telegram.memoiry.model.repository.StateRepository
import tech.ydb.table.SessionRetryContext

context(_: SessionRetryContext)
fun Dispatcher.text(): Unit = text {
    if (!checkSource()) return@text
    val userId = message.from?.id ?: return@text
    val state = StateRepository.findState(userId) ?: return@text
    when {
        state.state.isEmpty() -> {
            if (text.length > 255) {
                bot.sendMessage(ChatId.fromId(userId), "Слишком длинное название, исправьте или попробуйте еще раз")
                return@text
            }
            if (HintRepository.findUserHints(userId).any { it.key == text }) {
                StateRepository.delete(userId)
                bot.sendMessage(ChatId.fromId(userId), "Такая заметка уже существует, вы можете ее посмотреть /list")
                return@text
            }
            state.state[0] = text
            StateRepository.save(state)
            bot.sendMessage(ChatId.fromId(userId), "Что положить в заметку?")
        }

        state.state.lastKey() == 0 -> {
            HintRepository.saveHints(Hint(userId, state.state[0]!!, text))
            StateRepository.delete(userId)
            bot.sendMessage(ChatId.fromId(userId), "Заметка сохранена. Вы можете вызвать ее через inline режим, либо командой /list")
        }
    }
}

private fun TextHandlerEnvironment.checkSource(): Boolean {
    update.consume()
    if (update.message?.viaBot?.isBot == true) return false
    return message.chat.type == "private"
}
