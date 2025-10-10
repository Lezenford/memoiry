package com.lezenford.telegram.memoiry.model.repository

import com.lezenford.telegram.memoiry.model.entity.Hint
import kotlinx.coroutines.future.await
import tech.ydb.table.SessionRetryContext
import tech.ydb.table.query.Params
import tech.ydb.table.result.ResultSetReader
import tech.ydb.table.transaction.TxControl
import tech.ydb.table.values.PrimitiveValue

object HintRepository {
    context(client: SessionRetryContext)
    suspend fun findUserHints(userId: Long): List<Hint> {
        return client.supplyResult {
            it.executeDataQuery(
                $$"""
                      DECLARE $userId AS INT64;
        
                      SELECT user_id, key, value FROM hints WHERE user_id = $userId ORDER BY key;
                    """.trimIndent(),
                TxControl.serializableRw().setCommitTx(true),
                Params.of($$"$userId", PrimitiveValue.newInt64(userId))
            )
        }.await().value.getResultSet(0).toEntity()
    }

    context(client: SessionRetryContext)
    suspend fun saveHints(hint: Hint) {
        client.supplyResult {
            it.executeDataQuery(
                $$"""
                      DECLARE $userId AS INT64;
                      DECLARE $key AS UTF8;
                      DECLARE $value AS UTF8;
        
                      UPSERT INTO hints (user_id, key, value) VALUES ($userId, $key, $value);
                    """.trimIndent(),
                TxControl.serializableRw().setCommitTx(true),
                Params.of(
                    $$"$userId", PrimitiveValue.newInt64(hint.userId),
                    $$"$key", PrimitiveValue.newText(hint.key),
                    $$"$value", PrimitiveValue.newText(hint.value),
                )
            )
        }.await()
    }

    context(client: SessionRetryContext)
    suspend fun deleteHints(userId: Long, key: String) {
        client.supplyResult {
            it.executeDataQuery(
                $$"""
                      DECLARE $userId AS Int64;
                      DECLARE $key AS UTF8;
        
                      DELETE FROM hints WHERE user_id == $userId AND key == $key;
                    """.trimIndent(),
                TxControl.serializableRw().setCommitTx(true),
                Params.of(
                    $$"$userId", PrimitiveValue.newInt64(userId),
                    $$"$key", PrimitiveValue.newText(key),
                )
            )
        }.await()
    }

    private fun ResultSetReader.toEntity(): List<Hint> {
        val result = mutableListOf<Hint>()
        while (this.next()) {
            result.add(
                Hint(
                    userId = getColumn("user_id").int64,
                    key = getColumn("key").text,
                    value = getColumn("value").text,
                )
            )
        }
        return result
    }
}