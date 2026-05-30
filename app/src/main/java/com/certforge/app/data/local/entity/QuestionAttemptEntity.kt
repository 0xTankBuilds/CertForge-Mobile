package com.certforge.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "local_id") val localId: Long = 0,
    @ColumnInfo(name = "session_client_id") val sessionClientId: String,
    @ColumnInfo(name = "question_id") val questionId: String,
    @ColumnInfo(name = "selected_answer_index") val selectedAnswerIndex: Int,
    @ColumnInfo(name = "is_correct") val isCorrect: Boolean,
    @ColumnInfo(name = "time_spent_seconds") val timeSpentSeconds: Int
)
