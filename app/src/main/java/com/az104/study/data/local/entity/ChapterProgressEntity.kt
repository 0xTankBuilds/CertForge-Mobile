package com.az104.study.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey @ColumnInfo(name = "chapter_id") val chapterId: String,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "time_spent_seconds") val timeSpentSeconds: Int = 0,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
)
