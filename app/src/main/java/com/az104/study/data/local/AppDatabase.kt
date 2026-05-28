package com.az104.study.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.az104.study.data.local.dao.ArticleDao
import com.az104.study.data.local.dao.ChapterDao
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.DomainDao
import com.az104.study.data.local.dao.QuestionAttemptDao
import com.az104.study.data.local.dao.QuestionDao
import com.az104.study.data.local.dao.QuizSessionDao
import com.az104.study.data.local.dao.StudyGuideDao
import com.az104.study.data.local.dao.SyncMetadataDao
import com.az104.study.data.local.entity.ArticleEntity
import com.az104.study.data.local.entity.ChapterEntity
import com.az104.study.data.local.entity.ChapterProgressEntity
import com.az104.study.data.local.entity.DomainEntity
import com.az104.study.data.local.entity.QuestionAttemptEntity
import com.az104.study.data.local.entity.QuestionEntity
import com.az104.study.data.local.entity.QuizSessionEntity
import com.az104.study.data.local.entity.StudyGuideEntity
import com.az104.study.data.local.entity.SyncMetadataEntity

@Database(
    entities = [
        DomainEntity::class,
        ChapterEntity::class,
        QuestionEntity::class,
        StudyGuideEntity::class,
        ArticleEntity::class,
        QuizSessionEntity::class,
        QuestionAttemptEntity::class,
        ChapterProgressEntity::class,
        SyncMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun domainDao(): DomainDao
    abstract fun chapterDao(): ChapterDao
    abstract fun questionDao(): QuestionDao
    abstract fun studyGuideDao(): StudyGuideDao
    abstract fun articleDao(): ArticleDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun questionAttemptDao(): QuestionAttemptDao
    abstract fun chapterProgressDao(): ChapterProgressDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        const val DATABASE_NAME = "az104_study.db"
    }
}
