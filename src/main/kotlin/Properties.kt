package com.lezenford.telegram.memoiry

object Properties {
    val port: Int = System.getenv("PORT")?.toInt() ?: 8080
    val endpointPath: String = System.getenv("ENDPOINT_PATH")
    val telegramToken: String = System.getenv("TELEGRAM_TOKEN")
    val telegramSecretToken: String = System.getenv("TELEGRAM_SECRET_TOKEN")
    val connectionString: String = System.getenv("CONNECTION_STRING")
}