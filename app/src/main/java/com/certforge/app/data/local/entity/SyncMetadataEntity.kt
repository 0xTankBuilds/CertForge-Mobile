package com.certforge.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey @ColumnInfo(name = "data_type") val dataType: String,
    @ColumnInfo(name = "last_sync_timestamp") val lastSyncTimestamp: Long,
    val hash: String? = null
)
