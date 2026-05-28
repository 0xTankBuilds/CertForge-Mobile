package com.az104.study.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.az104.study.data.local.entity.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
    @Upsert
    suspend fun upsert(metadata: SyncMetadataEntity)

    @Query("SELECT * FROM sync_metadata WHERE data_type = :dataType")
    suspend fun getByType(dataType: String): SyncMetadataEntity?

    @Query("SELECT * FROM sync_metadata")
    suspend fun getAll(): List<SyncMetadataEntity>

    @Query("DELETE FROM sync_metadata")
    suspend fun deleteAll()
}
