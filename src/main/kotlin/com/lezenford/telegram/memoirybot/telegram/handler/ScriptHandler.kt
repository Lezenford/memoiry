package com.lezenford.telegram.memoirybot.telegram.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.ConcurrentHashMap

@Component
class ScriptHandler {
    private val availableScripts: MutableMap<Long, suspend (Message) -> BotApiMethod<*>?> = ConcurrentHashMap()

    suspend fun addScript(chatId: Long, action: suspend (Message) -> BotApiMethod<*>?) {
        availableScripts[chatId] = action
    }

    suspend fun invokeScript(message: Message): BotApiMethod<*>? =
        availableScripts.remove(message.chatId)?.invoke(message)
}