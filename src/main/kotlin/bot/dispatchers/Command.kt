package com.lezenford.telegram.memoiry.bot.dispatchers

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.lezenford.telegram.memoiry.model.entity.State
import com.lezenford.telegram.memoiry.model.repository.HintRepository
import com.lezenford.telegram.memoiry.model.repository.StateRepository
import tech.ydb.table.SessionRetryContext

enum class Command(
    val value: String,
    val description: String = "",
    val publish: Boolean = true
) {
    START("start", publish = false),
    HELP("help", "Список возможных команд"),
    ADD("add", "Добавить новую заметку"),
    LIST("list", "Список заметок")
}

fun Dispatcher.startCommand(): Unit = command(Command.START.value) {
    if (!checkSource()) return@command
    bot.sendMessage(ChatId.fromId(message.chat.id), "Привет! Ты можешь загружать в меня свои заметки. Подробности в /help")
}

fun Dispatcher.helpCommand(): Unit = command(Command.HELP.value) {
    if (!checkSource()) return@command
    bot.sendMessage(
        ChatId.fromId(message.chat.id), """
                Бот умеет возвращать сохраненные заметки через inline режим. 
                
                /add - добавить новую заметку 
                /list - посмотреть ваши заметки
            """.trimIndent()
    )
}

context(_: SessionRetryContext)
fun Dispatcher.listCommand(): Unit = command(Command.LIST.value) {
    if (!checkSource()) return@command
    val userId = message.from?.id ?: return@command
    val hints = HintRepository.findUserHints(userId)
    bot.sendMessage(
        ChatId.fromId(message.chat.id),
        text = if (hints.isEmpty()) "Нет заметок" else "Выберите заметку",
        replyMarkup = InlineKeyboardMarkup.create(hints.toKeyboard(userId))
    )
}

context(_: SessionRetryContext)
fun Dispatcher.addCommand(): Unit = command(Command.ADD.value) {
    if (!checkSource()) return@command
    val userId = message.from?.id ?: return@command
    StateRepository.save(State(userId, Command.ADD))
    bot.sendMessage(ChatId.fromId(message.chat.id), "Введите название для вашей заметки")

}

private fun CommandHandlerEnvironment.checkSource(): Boolean {
    update.consume()
    return message.chat.type == "private"
}