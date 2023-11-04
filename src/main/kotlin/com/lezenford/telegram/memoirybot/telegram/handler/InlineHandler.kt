package com.lezenford.telegram.memoirybot.telegram.handler

import com.lezenford.telegram.memoirybot.configuration.properties.TelegramProperties
import com.lezenford.telegram.memoirybot.extensions.PARSE_MODE
import com.lezenford.telegram.memoirybot.extensions.escape
import com.lezenford.telegram.memoirybot.service.HintService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import java.util.LinkedList

@Component
class InlineHandler(
    private val hintService: HintService,
    private val properties: TelegramProperties
) {

    suspend fun handle(inlineQuery: InlineQuery): AnswerInlineQuery {
        val userId = inlineQuery.from.id
        return hintService.list(userId, inlineQuery.query).take(properties.hintsCount).map {
            InlineQueryResultArticle.builder()
                .id(it.id.toString())
                .inputMessageContent(
                    InputTextMessageContent.builder()
                        .messageText(it.value.escape())
                        .parseMode(PARSE_MODE)
                        .build()
                ).title(it.key)
                .build()
        }.let {
            AnswerInlineQuery.builder()
                .cacheTime(0)
                .inlineQueryId(inlineQuery.id)
                .results(it.toCollection(LinkedList()))
                .build()
        }
    }
}