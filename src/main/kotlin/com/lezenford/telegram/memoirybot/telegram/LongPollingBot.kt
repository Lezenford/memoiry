package com.lezenford.telegram.memoirybot.telegram

import com.fasterxml.jackson.databind.ObjectMapper
import com.lezenford.telegram.memoirybot.configuration.properties.TelegramProperties
import com.lezenford.telegram.memoirybot.extensions.Logger
import com.lezenford.telegram.memoirybot.telegram.handler.CommandHandler
import com.lezenford.telegram.memoirybot.telegram.handler.InlineHandler
import com.lezenford.telegram.memoirybot.telegram.handler.ScriptHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

@Component
@ConditionalOnProperty(value = ["telegram.type"], havingValue = "long_polling")
class LongPollingBot(
    override val telegramWebClient: WebClient,
    override val objectMapper: ObjectMapper,
    override val properties: TelegramProperties,
    override val scriptHandler: ScriptHandler,
    override val commandHandler: CommandHandler,
    override val inlineHandler: InlineHandler
) : Bot(), DisposableBean {

    private val threadPool: ExecutorService = Executors.newSingleThreadExecutor()
    override val coroutineContext: CoroutineContext = threadPool.asCoroutineDispatcher()

    override fun run(vararg args: String?) {
        super.run(*args)
        runBlocking {
            sendMessage(DeleteWebhook())
        }
        receiveUpdates()
    }

    @Volatile
    private var offset: Int = 0

    private fun receiveUpdates() {
        CoroutineScope(coroutineContext).launch {
            delay(properties.longPolling.startDelay.toMillis())
            while (isActive) {
                try {
                    sendMessage(
                        GetUpdates.builder().also {
                            if (offset > 0) {
                                it.offset(offset)
                            }
                        }.build()
                    )?.onEach {
                        try {
                            receiveUpdate(it)?.also { answer ->
                                sendMessage(answer)
                            }
                        } catch (e: Exception) {
                            log.error("Receive update error: ${e.message}", e)
                        }
                        offset = max(offset, it.updateId + 1)
                    }
                } catch (e: Exception) {
                    log.error("Long polling mode error: ${e.message}", e)
                }
                delay(properties.longPolling.refreshDelay.toMillis())
            }
        }
    }

    override fun destroy() {
        coroutineContext.cancel()
        threadPool.shutdown()
    }

    companion object {
        private val log by Logger()
    }
}