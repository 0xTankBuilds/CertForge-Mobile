package com.certforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.certforge.app.data.local.entity.ChapterProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterProgressDao {
    @Upsert
    suspend fun upsert(progress: ChapterProgressEntity)

    @Upsert
    suspend fun upsertAll(progress: List<ChapterProgressEntity>)

    @Query("SELECT * FROM chapter_progress")
    fun observeAll(): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress WHERE chapter_id = :chapterId")
    suspend fun getByChapterId(chapterId: String): ChapterProgressEntity?

    @Query("SELECT * FROM chapter_progress WHERE is_synced = 0")
    suspend fun getUnsynced(): List<ChapterProgressEntity>

    @Query("UPDATE chapter_progress SET is_synced = 1 WHERE chapter_id = :chapterId")
    suspend fun markSynced(chapterId: String)

    @Query("SELECT COUNT(*) FROM chapter_progress WHERE completed_at IS NOT NULL")
    suspend fun countCompleted(): Int

    @Query("SELECT DISTINCT DATE(completed_at / 1000, 'unixepoch') FROM chapter_progress WHERE completed_at IS NOT NULL")
    suspend fun getCompletionDates(): List<String>
}
