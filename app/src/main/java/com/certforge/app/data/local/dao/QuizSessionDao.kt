package com.certforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.certforge.app.data.local.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizSessionDao {
    @Upsert
    suspend fun upsert(session: QuizSessionEntity)

    @Upsert
    suspend fun upsertAll(sessions: List<QuizSessionEntity>)

    @Query("SELECT * FROM quiz_sessions WHERE is_synced = 0")
    suspend fun getUnsynced(): List<QuizSessionEntity>

    @Query("SELECT * FROM quiz_sessions WHERE client_id = :clientId")
    suspend fun getByClientId(clientId: String): QuizSessionEntity?

    @Query("SELECT * FROM quiz_sessions ORDER BY started_at DESC")
    fun observeAll(): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM quiz_sessions ORDER BY started_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<QuizSessionEntity>

    @Query("UPDATE quiz_sessions SET is_synced = 1, server_id = :serverId WHERE client_id = :clientId")
    suspend fun markSynced(clientId: String, serverId: String?)

    @Query("SELECT COUNT(*) FROM quiz_sessions WHERE completed_at IS NOT NULL")
    suspend fun countCompleted(): Int

    @Query("DELETE FROM quiz_sessions")
    suspend fun deleteAll()
}
