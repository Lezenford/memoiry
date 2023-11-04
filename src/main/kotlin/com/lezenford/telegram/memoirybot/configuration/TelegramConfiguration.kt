package com.lezenford.telegram.memoirybot.configuration

import com.lezenford.telegram.memoirybot.configuration.properties.TelegramProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class TelegramConfiguration(
    private val properties: TelegramProperties
) {

    @Bean
    fun telegramWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.telegram.org/bot${properties.token}/")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}