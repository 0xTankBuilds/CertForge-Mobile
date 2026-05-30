package com.certforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.certforge.app.data.local.entity.CertificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificationDao {

    @Upsert
    suspend fun upsertAll(certifications: List<CertificationEntity>)

    @Query("SELECT * FROM certifications ORDER BY code")
    fun observeAll(): Flow<List<CertificationEntity>>

    @Query("SELECT * FROM certifications WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): CertificationEntity?

    @Query("SELECT * FROM certifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CertificationEntity?

    @Query("DELETE FROM certifications")
    suspend fun deleteAll()
}
