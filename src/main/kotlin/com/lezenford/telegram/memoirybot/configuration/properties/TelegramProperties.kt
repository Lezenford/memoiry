package com.lezenford.telegram.memoirybot.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.security.MessageDigest
import java.time.Duration
import java.time.temporal.Temporal
import java.util.Base64
import java.util.UUID

@ConfigurationProperties("telegram")
data class TelegramProperties @ConstructorBinding constructor(
    val token: String,
    val type: BotMode,
    val hintsCount: Int = 6,
    val secretToken: String = UUID.randomUUID().toString(),
    val webhook: WebHook = WebHook(),
    val longPolling: LongPolling = LongPolling()
){
    val tokenHash = Base64.getEncoder().encode(
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
    ).decodeToString().replace("/", "").replace("+", "").replace("=", "")

    data class WebHook(
        val url: String = "",
        val prefix: String = DEFAULT_PREFIX,
        val ipAddress: String? = null
    ){
        companion object {
            const val DEFAULT_PREFIX = "telegram"
        }
    }

    data class LongPolling(
        val startDelay: Duration = Duration.parse(DEFAULT_START_DELAY),
        val refreshDelay: Duration = Duration.parse(DEFAULT_REFRESH_DELAY)
    ){
        companion object {
            const val DEFAULT_START_DELAY = "PT2S"
            const val DEFAULT_REFRESH_DELAY = "PT0.5S"
        }
    }
}

enum class BotMode{
    WEBHOOK, LONG_POLLING
}