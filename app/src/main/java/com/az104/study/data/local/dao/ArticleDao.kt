package com.az104.study.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.az104.study.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface ArticleDao {
    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Upsert
    suspend fun upsert(article: ArticleEntity)

    @Query("SELECT * FROM articles ORDER BY title ASC")
    fun observeAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: String): ArticleEntity?

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int

    @Query("SELECT * FROM articles WHERE cached_at IS NOT NULL ORDER BY cached_at ASC")
    suspend fun getCachedArticles(): List<ArticleEntity>

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()
}
