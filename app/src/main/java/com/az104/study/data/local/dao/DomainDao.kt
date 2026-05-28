package com.az104.study.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.az104.study.data.local.entity.DomainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DomainDao {
    @Upsert
    suspend fun upsertAll(domains: List<DomainEntity>)

    @Upsert
    suspend fun upsert(domain: DomainEntity)

    @Query("SELECT * FROM domains ORDER BY module_id ASC")
    fun observeAll(): Flow<List<DomainEntity>>

    @Query("SELECT * FROM domains ORDER BY module_id ASC")
    suspend fun getAll(): List<DomainEntity>

    @Query("SELECT * FROM domains WHERE id = :id")
    suspend fun getById(id: String): DomainEntity?

    @Query("DELETE FROM domains")
    suspend fun deleteAll()
}
