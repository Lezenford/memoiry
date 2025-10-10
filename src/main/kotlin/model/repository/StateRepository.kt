package com.lezenford.telegram.memoiry.model.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lezenford.telegram.memoiry.bot.dispatchers.Command
import com.lezenford.telegram.memoiry.model.entity.State
import kotlinx.coroutines.future.await
import tech.ydb.table.SessionRetryContext
import tech.ydb.table.query.Params
import tech.ydb.table.result.ResultSetReader
import tech.ydb.table.transaction.TxControl
import tech.ydb.table.values.PrimitiveValue
import java.util.SortedMap

object StateRepository {
    context(client: SessionRetryContext)
    suspend fun findState(userId: Long): State? {
        return client.supplyResult {
            it.executeDataQuery(
                $$"""
                      DECLARE $userId AS Int64;
        
                      SELECT user_id, command, state FROM states WHERE user_id = $userId;
                    """.trimIndent(),
                TxControl.serializableRw().setCommitTx(true),
                Params.of($$"$userId", PrimitiveValue.newInt64(userId))
            )
        }.await().value.getResultSet(0).toEntity()
    }

    context(client: SessionRetryContext)
    suspend fun save(state: State) {
        client.supplyResult {
            it.executeDataQuery(
                $$"""
                      DECLARE $userId AS Int64;
                      DECLARE $command AS UTF8;
                      DECLARE $state AS JSON;
        
                      UPSERT INTO states (user_id, command, state) VALUES ($userId, $command, $state);
                    """.trimIndent(),
                TxControl.serializableRw().setCommitTx(true),
                Params.of(
                    $$"$userId", PrimitiveValue.newInt64(state.userId),
                    $$"$command", PrimitiveValue.newText(state.command.name),
                    $$"$state", PrimitiveValue.newJson(gson.toJson(state.state)),
                )
            )
        }.await()
    }

    context(client: SessionRetryContext)
    suspend fun delete(userId: Long) {
        client.supplyResult {
            it.executeDataQuery(
                $$"""
                      DECLARE $userId AS INT64;
        
                      DELETE FROM states WHERE user_id == $userId;
                    """.trimIndent(),
                TxControl.serializableRw().setCommitTx(true),
                Params.of($$"$userId", PrimitiveValue.newInt64(userId))
            )
        }.await()
    }

    private fun ResultSetReader.toEntity(): State? {
        while (this.next()) {
            return State(
                userId = getColumn("user_id").int64,
                command = Command.valueOf(getColumn("command").text),
                state = gson.fromJson(getColumn("state").json, object : TypeToken<SortedMap<Int, String>>() {}.type),
            )
        }
        return null
    }

    private val gson = Gson()
}