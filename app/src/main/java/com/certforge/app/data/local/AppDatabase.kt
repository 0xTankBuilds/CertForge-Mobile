package com.certforge.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.certforge.app.data.local.dao.ArticleDao
import com.certforge.app.data.local.dao.CertificationDao
import com.certforge.app.data.local.dao.ChapterDao
import com.certforge.app.data.local.dao.ChapterProgressDao
import com.certforge.app.data.local.dao.DomainDao
import com.certforge.app.data.local.dao.QuestionAttemptDao
import com.certforge.app.data.local.dao.QuestionDao
import com.certforge.app.data.local.dao.QuizSessionDao
import com.certforge.app.data.local.dao.StudyGuideDao
import com.certforge.app.data.local.dao.SyncMetadataDao
import com.certforge.app.data.local.entity.ArticleEntity
import com.certforge.app.data.local.entity.CertificationEntity
import com.certforge.app.data.local.entity.ChapterEntity
import com.certforge.app.data.local.entity.ChapterProgressEntity
import com.certforge.app.data.local.entity.DomainEntity
import com.certforge.app.data.local.entity.QuestionAttemptEntity
import com.certforge.app.data.local.entity.QuestionEntity
import com.certforge.app.data.local.entity.QuizSessionEntity
import com.certforge.app.data.local.entity.StudyGuideEntity
import com.certforge.app.data.local.entity.SyncMetadataEntity

@Database(
    entities = [
        CertificationEntity::class,
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
    abstract fun certificationDao(): CertificationDao
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
        const val DATABASE_NAME = "certforge.db"
    }
}
