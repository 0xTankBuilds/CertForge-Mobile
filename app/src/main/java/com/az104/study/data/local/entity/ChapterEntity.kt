package com.az104.study.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    @androidx.room.ColumnInfo(name = "domain_id") val domainId: String,
    val title: String,
    @androidx.room.ColumnInfo(name = "article_id") val articleId: String,
    @androidx.room.ColumnInfo(name = "content_url") val contentUrl: String
)
