package com.lezenford.telegram.memoiry.bot.dispatchers

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.lezenford.telegram.memoiry.model.entity.Hint
import com.lezenford.telegram.memoiry.model.repository.HintRepository
import tech.ydb.table.SessionRetryContext

enum class Operation(val value: String) {
    GET("get"),
    DELETE("delete"),
    BACK("back"),
    CLOSE("close")
}

private fun String.toOperation(): Operation = Operation.entries.first { it.value == this }

fun List<Hint>.toKeyboard(userId: Long) =
    map { listOf(InlineKeyboardButton.CallbackData(it.key, "${Operation.GET.value}_${userId}_${it.key}")) }
        .toMutableList().apply { add(listOf(InlineKeyboardButton.CallbackData("Закрыть", "${Operation.CLOSE.value}_${userId}"))) }

context(client: SessionRetryContext)
fun Dispatcher.callbackQuery(): Unit = callbackQuery {
    update.consume()
    bot.answerCallbackQuery(callbackQuery.id)
    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
    val messageId = callbackQuery.message?.messageId ?: return@callbackQuery
    val (operation, userId, key) =
        callbackQuery.data.split("_").let { Triple(it[0].toOperation(), it[1].toLong(), it.getOrNull(2)) }
    val hints = HintRepository.findUserHints(userId)
    when (operation) {
        Operation.GET -> {
            val hint = hints.find { it.key == key } ?: return@callbackQuery
            bot.editMessageText(
                ChatId.fromId(chatId),
                messageId = messageId,
                text = """
                ${hint.key}
                
                ${hint.value}
            """.trimIndent(),
                replyMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData("Удалить", "${Operation.DELETE.value}_${userId}_$key")),
                    listOf(InlineKeyboardButton.CallbackData("Назад", "${Operation.BACK.value}_$userId"))
                )
            )
        }

        Operation.DELETE -> {
            HintRepository.deleteHints(userId, key!!)
            bot.editMessageText(
                ChatId.fromId(chatId),
                messageId = messageId,
                text = "Заметка удалена".trimIndent(),
                replyMarkup = InlineKeyboardMarkup.create()
            )
        }

        Operation.BACK -> {
            bot.editMessageText(
                ChatId.fromId(chatId),
                messageId = messageId,
                text = "Выберите заметку",
                replyMarkup = InlineKeyboardMarkup.create(hints.toKeyboard(userId))
            )
        }

        Operation.CLOSE -> {
            bot.editMessageText(
                ChatId.fromId(chatId),
                messageId = messageId,
                text = "Список заметок доступен по команде /list",
                replyMarkup = InlineKeyboardMarkup.create()
            )
        }
    }
}