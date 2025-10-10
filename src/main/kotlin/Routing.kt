package com.lezenford.telegram.memoiry

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.BotCommand
import com.lezenford.telegram.memoiry.bot.dispatchers.Command
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

private const val TELEGRAM_BOT_API_SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token"

context(bot: Bot)
fun Application.configureRouting() {
    routing {
        post("/telegram/${Properties.telegramToken}") {
            if (call.request.header(TELEGRAM_BOT_API_SECRET_TOKEN_HEADER) != Properties.telegramSecretToken) {
                call.respond(HttpStatusCode.BadRequest)
            }
            val receiveText = call.receiveText()
            log.info("Receive update: $receiveText")
            bot.processUpdate(receiveText)
            call.respond(HttpStatusCode.OK)
        }
        put("/telegram/${Properties.telegramToken}") {
            bot.setWebhook(
                url = "${Properties.endpointPath}telegram/${Properties.telegramToken}",
                secretToken = Properties.telegramSecretToken
            )
            bot.setMyCommands(Command.entries.filter { it.publish }.map { BotCommand(it.value, it.description) })
            call.respond(HttpStatusCode.OK)
        }
    }
}
