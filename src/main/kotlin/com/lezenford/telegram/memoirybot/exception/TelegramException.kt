package com.lezenford.telegram.memoirybot.exception

import org.telegram.telegrambots.meta.api.objects.ApiResponse

class TelegramApiException(response: String): RuntimeException(response) {
    constructor(response: ApiResponse<Unit>) : this(response.toString())
}