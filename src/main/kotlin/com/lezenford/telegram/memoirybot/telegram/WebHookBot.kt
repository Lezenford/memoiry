package com.lezenford.telegram.memoirybot.telegram

import com.fasterxml.jackson.databind.ObjectMapper
import com.lezenford.telegram.memoirybot.configuration.properties.TelegramProperties
import com.lezenford.telegram.memoirybot.extensions.Logger
import com.lezenford.telegram.memoirybot.telegram.handler.CommandHandler
import com.lezenford.telegram.memoirybot.telegram.handler.InlineHandler
import com.lezenford.telegram.memoirybot.telegram.handler.ScriptHandler
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.reactive.function.client.WebClient
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.UUID

@Component
@ConditionalOnProperty(value = ["telegram.type"], havingValue = "webhook")
class WebHookBot(
    override val telegramWebClient: WebClient,
    override val objectMapper: ObjectMapper,
    override val properties: TelegramProperties,
    override val scriptHandler: ScriptHandler,
    override val commandHandler: CommandHandler,
    override val inlineHandler: InlineHandler
) : Bot() {

    override fun run(vararg args: String?) {
        super.run(*args)
        runBlocking {
            sendMessage(
                SetWebhook.builder()
                    .secretToken(properties.secretToken)
                    .url("${properties.webhook.url}/${properties.webhook.prefix}/${properties.tokenHash}")
                    .build()
            )
        }
    }

    @PostMapping("/{token}")
    suspend fun webhook(
        @RequestHeader(required = true, name = TELEGRAM_BOT_API_SECRET_TOKEN_HEADER) secretHeader: String,
        @PathVariable token: String,
        @RequestBody update: Update
    ): BotApiMethod<*>? {
        try {
            if (validate(secretHeader, token)) {
                val requestId = UUID.randomUUID()
                log.debug("Request $requestId: ${objectMapper.writeValueAsString(update)}")
                return receiveUpdate(update)?.also {
                    log.debug("Response $requestId: ${objectMapper.writeValueAsString(it)}")
                }
            }
        } catch (e: Exception) {
            log.error("Receive message error: ${objectMapper.writeValueAsString(update)}", e)
        }
        return null
    }

    private fun validate(secretHeader: String, token: String): Boolean {
        return token == properties.tokenHash && secretHeader == properties.secretToken
    }

    companion object {
        private val log by Logger()
        private const val TELEGRAM_BOT_API_SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token"
    }
}