package com.certforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.certforge.app.data.local.entity.QuestionAttemptEntity

@Dao
interface QuestionAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attempts: List<QuestionAttemptEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuestionAttemptEntity)

    @Query("SELECT * FROM question_attempts WHERE session_client_id = :sessionClientId ORDER BY local_id ASC")
    suspend fun getBySession(sessionClientId: String): List<QuestionAttemptEntity>

    @Query("SELECT * FROM question_attempts WHERE session_client_id IN (SELECT client_id FROM quiz_sessions WHERE is_synced = 0)")
    suspend fun getUnsyncedAttempts(): List<QuestionAttemptEntity>

    @Query("SELECT COUNT(*) FROM question_attempts")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM question_attempts WHERE is_correct = 1")
    suspend fun countCorrect(): Int
}
