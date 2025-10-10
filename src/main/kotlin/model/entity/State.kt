package com.lezenford.telegram.memoiry.model.entity

import com.lezenford.telegram.memoiry.bot.dispatchers.Command
import java.util.SortedMap
import java.util.TreeMap

data class State(
    val userId: Long,
    val command: Command,
    val state: SortedMap<Int, String> = TreeMap()
)