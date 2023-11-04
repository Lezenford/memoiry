package com.lezenford.telegram.memoirybot.extensions

const val PARSE_MODE = "MarkdownV2"

fun String.escape(): String = this.map { if (it.code in 1..126) "\\$it" else it }.joinToString("")