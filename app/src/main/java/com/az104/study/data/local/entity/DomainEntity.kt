package com.az104.study.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "domains")
data class DomainEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val weight: Int,
    @ColumnInfo(name = "module_id") val moduleId: String,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long = System.currentTimeMillis()
)
