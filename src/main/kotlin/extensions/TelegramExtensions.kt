package com.lezenford.telegram.memoiry.extensions

fun String.escape(): String = this.map { if (it.code in 1..126) "\\$it" else it }.joinToString("")