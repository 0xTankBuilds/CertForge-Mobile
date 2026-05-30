package com.certforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certifications")
data class CertificationEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val description: String
)
