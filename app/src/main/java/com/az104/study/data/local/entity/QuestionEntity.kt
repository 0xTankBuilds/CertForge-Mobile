package com.az104.study.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "domain_id") val domainId: String?,
    val type: String,
    @ColumnInfo(name = "question_text") val questionText: String,
    val options: List<String>,
    @ColumnInfo(name = "correct_answer_index") val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String,
    @ColumnInfo(name = "source_reference") val sourceReference: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long?
)
