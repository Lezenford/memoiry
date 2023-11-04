package com.lezenford.telegram.memoirybot.telegram.command

import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

abstract class Command<T: BotApiMethod<*>> {
    abstract val command: String
    open val description: String = ""
    open val publish: Boolean = false
    abstract suspend fun action(update: Update): T?

    companion object {
        const val COMMAND_INIT_CHARACTER = "/"
    }
}