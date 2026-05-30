package com.certforge.app.di

import android.content.Context
import androidx.room.Room
import com.certforge.app.data.local.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideCertificationDao(db: AppDatabase): CertificationDao = db.certificationDao()
    @Provides fun provideDomainDao(db: AppDatabase): DomainDao = db.domainDao()
    @Provides fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao()
    @Provides fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()
    @Provides fun provideStudyGuideDao(db: AppDatabase): StudyGuideDao = db.studyGuideDao()
    @Provides fun provideArticleDao(db: AppDatabase): ArticleDao = db.articleDao()
    @Provides fun provideQuizSessionDao(db: AppDatabase): QuizSessionDao = db.quizSessionDao()
    @Provides fun provideQuestionAttemptDao(db: AppDatabase): QuestionAttemptDao = db.questionAttemptDao()
    @Provides fun provideChapterProgressDao(db: AppDatabase): ChapterProgressDao = db.chapterProgressDao()
    @Provides fun provideSyncMetadataDao(db: AppDatabase): SyncMetadataDao = db.syncMetadataDao()
}
