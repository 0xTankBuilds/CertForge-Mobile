package com.az104.study.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.az104.study.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Upsert
    suspend fun upsertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions ORDER BY created_at ASC")
    suspend fun getAll(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE domain_id = :domainId ORDER BY created_at ASC")
    suspend fun getByDomain(domainId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: String): QuestionEntity?

    @Query("SELECT COUNT(*) FROM questions WHERE domain_id = :domainId")
    suspend fun countByDomain(domainId: String): Int

    @Query("SELECT COUNT(*) FROM questions")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    @Query("SELECT MAX(updated_at) FROM questions")
    suspend fun getMaxUpdatedAt(): Long?
}
