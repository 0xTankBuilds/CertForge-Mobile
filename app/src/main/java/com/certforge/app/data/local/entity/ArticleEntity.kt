package com.certforge.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    @ColumnInfo(name = "domain_id") val domainId: String,
    val html: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long? = null
)
