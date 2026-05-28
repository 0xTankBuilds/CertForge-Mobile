package com.az104.study.data.repository

import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.QuestionAttemptDao
import com.az104.study.data.local.dao.QuestionDao
import com.az104.study.data.local.dao.QuizSessionDao
import com.az104.study.data.local.entity.ChapterProgressEntity
import com.az104.study.data.local.entity.QuestionAttemptEntity
import com.az104.study.data.local.entity.QuestionEntity
import com.az104.study.data.local.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

data class QuizQuestion(
    val id: String,
    val type: String,
    val questionText: String,
    val options: List<String>,
    val difficulty: String,
    val domainId: String?
)

data class QuizResult(
    val score: Int,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctCount: Int,
    val attempts: List<QuestionAttemptEntity>,
    val questions: Map<String, QuestionEntity>
)

class QuizRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val quizSessionDao: QuizSessionDao,
    private val questionAttemptDao: QuestionAttemptDao,
    private val chapterProgressDao: ChapterProgressDao
) {
    fun observeSessions(): Flow<List<QuizSessionEntity>> = quizSessionDao.observeAll()

    suspend fun getQuestions(domainId: String? = null, excludeIds: List<String> = emptyList()): List<QuestionEntity> {
        val questions = if (domainId != null) {
            questionDao.getByDomain(domainId)
        } else {
            questionDao.getAll()
        }
        return questions.filter { it.id !in excludeIds }.shuffled()
    }

    suspend fun createSession(
        type: String,
        domainId: String?,
        totalQuestions: Int
    ): QuizSessionEntity {
        val session = QuizSessionEntity(
            clientId = UUID.randomUUID().toString(),
            type = type,
            domainId = domainId,
            totalQuestions = totalQuestions,
            answeredQuestions = 0,
            startedAt = System.currentTimeMillis()
        )
        quizSessionDao.upsert(session)
        return session
    }

    suspend fun submitAnswer(
        sessionClientId: String,
        questionId: String,
        selectedAnswerIndex: Int,
        timeSpentSeconds: Int,
        isCorrect: Boolean
    ) {
        val attempt = QuestionAttemptEntity(
            sessionClientId = sessionClientId,
            questionId = questionId,
            selectedAnswerIndex = selectedAnswerIndex,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds
        )
        questionAttemptDao.insert(attempt)

        // Update session progress
        val session = quizSessionDao.getByClientId(sessionClientId) ?: return
        val attempts = questionAttemptDao.getBySession(sessionClientId)
        val answered = attempts.size
        val allCorrect = attempts.all { it.isCorrect }

        val updatedSession = session.copy(
            answeredQuestions = answered,
            score = if (answered >= session.totalQuestions) {
                (attempts.count { it.isCorrect } * 100) / answered
            } else null,
            completedAt = if (answered >= session.totalQuestions) System.currentTimeMillis() else null
        )
        quizSessionDao.upsert(updatedSession)
    }

    suspend fun getResult(sessionClientId: String): QuizResult? {
        val session = quizSessionDao.getByClientId(sessionClientId) ?: return null
        val attempts = questionAttemptDao.getBySession(sessionClientId)
        val questionIds = attempts.map { it.questionId }
        val allQuestions = questionDao.getAll()
        val questionMap = allQuestions.filter { it.id in questionIds }.associateBy { it.id }
        val correctCount = attempts.count { it.isCorrect }

        return QuizResult(
            score = if (attempts.isNotEmpty()) (correctCount * 100) / attempts.size else 0,
            totalQuestions = session.totalQuestions,
            answeredQuestions = attempts.size,
            correctCount = correctCount,
            attempts = attempts,
            questions = questionMap
        )
    }

    suspend fun markChapterComplete(chapterId: String, timeSpentSeconds: Int) {
        val existing = chapterProgressDao.getByChapterId(chapterId)
        if (existing != null) {
            chapterProgressDao.upsert(
                existing.copy(
                    completedAt = System.currentTimeMillis(),
                    timeSpentSeconds = existing.timeSpentSeconds + timeSpentSeconds,
                    isSynced = false
                )
            )
        } else {
            chapterProgressDao.upsert(
                ChapterProgressEntity(
                    chapterId = chapterId,
                    completedAt = System.currentTimeMillis(),
                    timeSpentSeconds = timeSpentSeconds,
                    isSynced = false
                )
            )
        }
    }

    suspend fun markChapterIncomplete(chapterId: String) {
        val existing = chapterProgressDao.getByChapterId(chapterId)
        if (existing != null) {
            chapterProgressDao.upsert(existing.copy(completedAt = null, isSynced = false))
        } else {
            chapterProgressDao.upsert(
                ChapterProgressEntity(chapterId = chapterId, isSynced = false)
            )
        }
    }

    suspend fun getChapterProgress(chapterId: String): ChapterProgressEntity? =
        chapterProgressDao.getByChapterId(chapterId)

    fun observeChapterProgress(): Flow<List<ChapterProgressEntity>> =
        chapterProgressDao.observeAll()

    suspend fun getSessions(): List<QuizSessionEntity> =
        quizSessionDao.getRecent(50)

    suspend fun getTotalQuestions(): Int = questionDao.getAll().size
    suspend fun getTotalAttempts(): Int = questionAttemptDao.count()
    suspend fun getCorrectAttempts(): Int = questionAttemptDao.countCorrect()
    suspend fun getChaptersCompleted(): Int = chapterProgressDao.countCompleted()
}
