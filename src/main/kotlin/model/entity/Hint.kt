package com.lezenford.telegram.memoiry.model.entity

data class Hint(
    val userId: Long,
    val key: String,
    val value: String
)