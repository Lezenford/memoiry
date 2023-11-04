package com.lezenford.telegram.memoirybot.service

import com.lezenford.telegram.memoirybot.model.entity.Hint
import com.lezenford.telegram.memoirybot.model.repository.HintRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HintService(
    private val hintRepository: HintRepository,
) {

    suspend fun list(userId: Long, keyPart: String = ""): Flow<Hint> {
        return hintRepository.findAllByUserId(userId).filter { keyPart.lowercase() in it.key.lowercase() }
    }

    suspend fun find(userId: Long, key: String): Hint? {
        return hintRepository.findByUserIdAndKey(userId, key)
    }

    @Transactional
    suspend fun delete(hint: Hint) {
        hintRepository.delete(hint)
    }

    @Transactional
    suspend fun save(hint: Hint):Hint {
      return  hintRepository.save(hint)
    }
}