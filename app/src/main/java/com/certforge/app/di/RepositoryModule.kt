package com.certforge.app.di

import android.content.Context
import com.certforge.app.data.remote.SyncApi
import com.certforge.app.data.repository.ContentRepository
import com.certforge.app.data.repository.QuizRepository
import com.certforge.app.data.repository.SyncRepository
import com.certforge.app.data.local.dao.ArticleDao
import com.certforge.app.data.local.dao.ChapterDao
import com.certforge.app.data.local.dao.ChapterProgressDao
import com.certforge.app.data.local.dao.DomainDao
import com.certforge.app.data.local.dao.QuestionAttemptDao
import com.certforge.app.data.local.dao.QuestionDao
import com.certforge.app.data.local.dao.QuizSessionDao
import com.certforge.app.data.local.dao.StudyGuideDao
import com.certforge.app.data.local.dao.SyncMetadataDao
import com.certforge.app.util.ServerUrlManager
import com.certforge.app.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RepositoryModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideServerUrlManager(@ApplicationContext context: Context): ServerUrlManager {
        return ServerUrlManager(context)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        syncApi: SyncApi,
        domainDao: DomainDao,
        chapterDao: ChapterDao,
        questionDao: QuestionDao,
        studyGuideDao: StudyGuideDao,
        articleDao: ArticleDao,
        quizSessionDao: QuizSessionDao,
        questionAttemptDao: QuestionAttemptDao,
        chapterProgressDao: ChapterProgressDao,
        syncMetadataDao: SyncMetadataDao,
        serverUrlManager: ServerUrlManager
    ): SyncRepository {
        return SyncRepository(
            syncApi = syncApi,
            domainDao = domainDao,
            chapterDao = chapterDao,
            questionDao = questionDao,
            studyGuideDao = studyGuideDao,
            articleDao = articleDao,
            quizSessionDao = quizSessionDao,
            questionAttemptDao = questionAttemptDao,
            chapterProgressDao = chapterProgressDao,
            syncMetadataDao = syncMetadataDao,
            serverUrlManager = serverUrlManager
        )
    }

    @Provides
    @Singleton
    fun provideContentRepository(
        syncApi: SyncApi,
        articleDao: ArticleDao,
        studyGuideDao: StudyGuideDao,
        domainDao: DomainDao,
        chapterDao: ChapterDao,
        serverUrlManager: ServerUrlManager
    ): ContentRepository {
        return ContentRepository(
            syncApi = syncApi,
            articleDao = articleDao,
            studyGuideDao = studyGuideDao,
            domainDao = domainDao,
            chapterDao = chapterDao,
            serverUrlManager = serverUrlManager
        )
    }

    @Provides
    @Singleton
    fun provideQuizRepository(
        questionDao: QuestionDao,
        quizSessionDao: QuizSessionDao,
        questionAttemptDao: QuestionAttemptDao,
        chapterProgressDao: ChapterProgressDao
    ): QuizRepository {
        return QuizRepository(
            questionDao = questionDao,
            quizSessionDao = quizSessionDao,
            questionAttemptDao = questionAttemptDao,
            chapterProgressDao = chapterProgressDao
        )
    }
}
