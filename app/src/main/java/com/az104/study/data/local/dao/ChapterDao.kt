package com.az104.study.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.az104.study.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Upsert
    suspend fun upsertAll(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun observeAll(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE domain_id = :domainId ORDER BY id ASC")
    fun observeByDomain(domainId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getById(id: String): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE article_id = :articleId LIMIT 1")
    suspend fun getByArticleId(articleId: String): ChapterEntity?

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    suspend fun getAll(): List<ChapterEntity>

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun count(): Int

    @Query("DELETE FROM chapters")
    suspend fun deleteAll()
}
