package com.lezenford.telegram.memoiry.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.webhook
import com.lezenford.telegram.memoiry.Properties
import com.lezenford.telegram.memoiry.bot.dispatchers.addCommand
import com.lezenford.telegram.memoiry.bot.dispatchers.callbackQuery
import com.lezenford.telegram.memoiry.bot.dispatchers.helpCommand
import com.lezenford.telegram.memoiry.bot.dispatchers.inlineRequest
import com.lezenford.telegram.memoiry.bot.dispatchers.listCommand
import com.lezenford.telegram.memoiry.bot.dispatchers.startCommand
import com.lezenford.telegram.memoiry.bot.dispatchers.text
import tech.ydb.table.SessionRetryContext

context(_: SessionRetryContext)
fun Bot.Builder.init() {
    token = Properties.telegramToken
    logLevel = LogLevel.Error
    webhook {
        url = "${Properties.endpointPath}telegram/$token"
        secretToken = Properties.telegramSecretToken
        createOnStart = false
    }
    dispatch {
        inlineRequest()

        startCommand()
        helpCommand()
        listCommand()
        addCommand()

        callbackQuery()

        text()
    }
}