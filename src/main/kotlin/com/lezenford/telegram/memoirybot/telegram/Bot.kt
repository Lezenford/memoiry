package com.lezenford.telegram.memoirybot.telegram

import com.fasterxml.jackson.databind.ObjectMapper
import com.lezenford.telegram.memoirybot.configuration.properties.TelegramProperties
import com.lezenford.telegram.memoirybot.exception.TelegramApiException
import com.lezenford.telegram.memoirybot.extensions.Logger
import com.lezenford.telegram.memoirybot.telegram.handler.CommandHandler
import com.lezenford.telegram.memoirybot.telegram.handler.InlineHandler
import com.lezenford.telegram.memoirybot.telegram.handler.ScriptHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchangeOrNull
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.ApiResponse
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

abstract class Bot : CommandLineRunner, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    protected abstract val telegramWebClient: WebClient
    protected abstract val objectMapper: ObjectMapper
    protected abstract val properties: TelegramProperties
    protected abstract val scriptHandler: ScriptHandler
    protected abstract val commandHandler: CommandHandler
    protected abstract val inlineHandler: InlineHandler

    override fun run(vararg args: String?) {
        runBlocking {
            commandHandler.publishCommands(this@Bot)
        }
    }

    protected suspend fun receiveUpdate(update: Update): BotApiMethod<*>? {
        try {
            when {
                // inline queries
                update.hasInlineQuery() -> return inlineHandler.handle(update.inlineQuery)

                // working only with personal messages except inline queries
                update.hasMessage() && update.message.from.id == update.message.chatId -> when {
                    // command invoke
                    update.hasMessage() && update.message.isCommand -> return commandHandler.handle(update)

                    // invoke script if exist
                    update.hasMessage() -> return scriptHandler.invokeScript(update.message)
                }
            }
        } catch (e: Exception) {
            log.error("Error while processing message: ${objectMapper.writeValueAsString(update)}", e)
        }
        return null
    }

    suspend fun <T : Serializable> sendMessage(message: BotApiMethod<T>): T? {
        log.info("Client request ${objectMapper.writeValueAsString(message)}")
        return telegramWebClient.post()
            .uri(message.method)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(message)
            .awaitExchangeOrNull { response ->
                when {
                    response.statusCode().is2xxSuccessful -> {
                        val awaitBody = response.awaitBody<String>()
                        log.info("Telegram response: $awaitBody")
                        message.deserializeResponse(awaitBody)
                    }
                    response.statusCode().is4xxClientError -> {
                        throw TelegramApiException(response.awaitBody<ApiResponse<Unit>>())
                    }

                    else -> throw TelegramApiException("Error while request telegram api: " + response.awaitBody())
                }
            }
    }

    companion object {
        private val log by Logger()
    }
}