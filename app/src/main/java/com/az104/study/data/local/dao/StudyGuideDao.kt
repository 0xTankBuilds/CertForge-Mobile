package com.az104.study.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.az104.study.data.local.entity.StudyGuideEntity

@Dao
interface StudyGuideDao {
    @Upsert
    suspend fun upsertAll(guides: List<StudyGuideEntity>)

    @Query("SELECT * FROM study_guides WHERE article_id = :articleId")
    suspend fun getByArticleId(articleId: String): StudyGuideEntity?

    @Query("SELECT * FROM study_guides ORDER BY created_at ASC")
    suspend fun getAll(): List<StudyGuideEntity>

    @Query("SELECT COUNT(*) FROM study_guides")
    suspend fun count(): Int

    @Query("DELETE FROM study_guides")
    suspend fun deleteAll()
}
