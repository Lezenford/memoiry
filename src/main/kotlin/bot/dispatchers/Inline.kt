package com.lezenford.telegram.memoiry.bot.dispatchers

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.inlineQuery
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.inlinequeryresults.InlineQueryResult
import com.github.kotlintelegrambot.entities.inlinequeryresults.InputMessageContent
import com.lezenford.telegram.memoiry.extensions.escape
import com.lezenford.telegram.memoiry.model.repository.HintRepository
import tech.ydb.table.SessionRetryContext

context(_: SessionRetryContext)
fun Dispatcher.inlineRequest(): Unit = inlineQuery {
    update.consume()
    val hints = HintRepository.findUserHints(inlineQuery.from.id)
    bot.answerInlineQuery(
        inlineQuery.id, hints.map {
            InlineQueryResult.Article(
                id = "${inlineQuery.from.id}_${it.key}",
                title = it.key,
                inputMessageContent = InputMessageContent.Text(it.value.escape(), ParseMode.MARKDOWN_V2)
            )
        },
        isPersonal = true,
        cacheTime = 10
    )
}