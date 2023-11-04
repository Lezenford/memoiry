package com.lezenford.telegram.memoirybot.model.repository

import com.lezenford.telegram.memoirybot.model.entity.Hint
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface HintRepository: CoroutineCrudRepository<Hint, Int> {
    fun findAllByUserId(userId: Long): Flow<Hint>

    suspend fun findByUserIdAndKey(userId: Long, key: String): Hint?
}