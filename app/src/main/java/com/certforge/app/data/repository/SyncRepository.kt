package com.certforge.app.data.repository

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
import com.certforge.app.data.local.entity.ChapterEntity
import com.certforge.app.data.local.entity.DomainEntity
import com.certforge.app.data.local.entity.QuestionEntity
import com.certforge.app.data.local.entity.CertificationEntity
import com.certforge.app.data.local.entity.SyncMetadataEntity
import com.certforge.app.data.remote.ProgressUploadRequest
import com.certforge.app.data.remote.SyncApi
import com.certforge.app.data.remote.UploadAttemptDto
import com.certforge.app.data.remote.UploadChapterDto
import com.certforge.app.data.remote.UploadSessionDto
import com.certforge.app.util.ServerUrlManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

data class SyncResult(
    val success: Boolean,
    val message: String = "",
    val sessionsUploaded: Int = 0,
    val chaptersUploaded: Int = 0
)

class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val certificationDao: CertificationDao,
    private val domainDao: DomainDao,
    private val chapterDao: ChapterDao,
    private val questionDao: QuestionDao,
    private val studyGuideDao: StudyGuideDao,
    private val articleDao: ArticleDao,
    private val quizSessionDao: QuizSessionDao,
    private val questionAttemptDao: QuestionAttemptDao,
    private val chapterProgressDao: ChapterProgressDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val serverUrlManager: ServerUrlManager
) {

    suspend fun isPaired(): Boolean {
        return serverUrlManager.getServerUrl() != null && serverUrlManager.getProfileId() != null
    }

    /**
     * Fetch available certifications from server and cache locally.
     */
    suspend fun fetchCertifications(): List<CertificationEntity> {
        val response = syncApi.getCertifications()
        val entities = response.certifications.map { dto ->
            CertificationEntity(
                id = dto.id,
                code = dto.code,
                name = dto.name,
                description = dto.description
            )
        }
        certificationDao.deleteAll()
        certificationDao.upsertAll(entities)
        return entities
    }

    /**
     * Switch to a different certification, clearing old content and re-syncing.
     */
    suspend fun switchCertification(certId: String) {
        serverUrlManager.setSelectedCertId(certId)

        // Clear old content data
        domainDao.deleteAll()
        chapterDao.deleteAll()
        questionDao.deleteAll() // questions use upsert, not delete — explicitly clear
        studyGuideDao.deleteAll()
        articleDao.deleteAll()
        syncMetadataDao.deleteAll()

        // Re-sync everything for the new cert
        performSync(isManual = true)
    }

    /**
     * Full sync: manifest check → download changed data → upload local progress → download remote progress.
     */
    suspend fun performSync(isManual: Boolean = false): SyncResult {
        try {
            val profileId = serverUrlManager.getProfileId() ?: return SyncResult(false, "Not paired")
            val certId = serverUrlManager.getSelectedCertId()

            // 1. Get manifest
            val manifest = syncApi.getManifest(certId = certId)
            val hashes = manifest.manifest
            val serverTime = manifest.serverTime

            // 2. Download changed data
            downloadChangedDomains(hashes["domains"], certId)
            downloadChangedQuestions(hashes["questions"], certId)
            downloadChangedStudyGuides(hashes["studyGuides"], certId)
            downloadChangedArticles(hashes["articles"], certId)

            // 3. Upload local progress
            val progressResult = uploadLocalProgress(profileId)

            // 4. Download remote progress
            downloadRemoteProgress(profileId)

            serverUrlManager.setLastSyncTimestamp(serverTime)

            return SyncResult(
                success = true,
                sessionsUploaded = progressResult.sessionsUploaded,
                chaptersUploaded = progressResult.chaptersUploaded
            )
        } catch (e: Exception) {
            return SyncResult(false, e.message ?: "Sync failed")
        }
    }

    private suspend fun downloadChangedDomains(currentHash: String?, certId: String? = null) {
        if (currentHash == null) return

        val stored = syncMetadataDao.getByType("domains")
        if (stored?.hash == currentHash) return // unchanged

        val response = syncApi.getDomains(certId = certId ?: serverUrlManager.getSelectedCertId())
        val now = System.currentTimeMillis()

        domainDao.deleteAll()
        domainDao.upsertAll(response.domains.map { d ->
            DomainEntity(
                id = d.id,
                name = d.name,
                description = d.description,
                weight = d.weight,
                moduleId = d.moduleId,
                lastSyncedAt = now,
                certId = certId ?: serverUrlManager.getSelectedCertId()
            )
        })

        chapterDao.deleteAll()
        chapterDao.upsertAll(response.domains.flatMap { d ->
            d.chapters.map { c ->
                ChapterEntity(
                    id = c.id,
                    domainId = d.id,
                    title = c.title,
                    articleId = c.articleId,
                    contentUrl = c.contentUrl
                )
            }
        })

        syncMetadataDao.upsert(SyncMetadataEntity("domains", now, currentHash))
    }

    private suspend fun downloadChangedQuestions(currentHash: String?, certId: String? = null) {
        if (currentHash == null) return

        val stored = syncMetadataDao.getByType("questions")
        if (stored?.hash == currentHash) return // unchanged

        val cid = certId ?: serverUrlManager.getSelectedCertId()
        // Incremental if stored timestamp exists
        val since = stored?.lastSyncTimestamp
        val response = if (since != null && since > 0) {
            syncApi.getQuestions(since = since, certId = cid)
        } else {
            syncApi.getQuestions(certId = cid)
        }

        if (response.questions.isEmpty()) return

        // Always re-download all for simplicity (question set is small)
        // In a future optimization, use incremental upsert
        questionDao.upsertAll(response.questions.map { q ->
            QuestionEntity(
                id = q.id,
                domainId = q.domainId,
                type = q.type,
                questionText = q.questionText,
                options = q.options,
                correctAnswerIndex = q.correctAnswerIndex,
                explanation = q.explanation,
                difficulty = q.difficulty,
                sourceReference = q.sourceReference,
                createdAt = q.createdAt,
                updatedAt = q.updatedAt
            )
        })

        syncMetadataDao.upsert(SyncMetadataEntity("questions", response.timestamp, currentHash))
    }

    private suspend fun downloadChangedStudyGuides(currentHash: String?, certId: String? = null) {
        if (currentHash == null) return

        val stored = syncMetadataDao.getByType("studyGuides")
        if (stored?.hash == currentHash) return

        val cid = certId ?: serverUrlManager.getSelectedCertId()
        val since = stored?.lastSyncTimestamp
        val response = if (since != null && since > 0) {
            syncApi.getStudyGuides(since = since, certId = cid)
        } else {
            syncApi.getStudyGuides(certId = cid)
        }

        if (response.studyGuides.isEmpty()) return

        studyGuideDao.deleteAll()
        val guides = response.studyGuides.map { g ->
            com.certforge.app.data.local.entity.StudyGuideEntity(
                id = g.id,
                articleId = g.articleId,
                title = g.title,
                content = g.content,
                createdAt = g.createdAt,
                updatedAt = g.updatedAt
            )
        }
        studyGuideDao.upsertAll(guides)

        syncMetadataDao.upsert(SyncMetadataEntity("studyGuides", response.timestamp, currentHash))
    }

    private suspend fun downloadChangedArticles(currentHash: String?, certId: String? = null) {
        if (currentHash == null) return

        val stored = syncMetadataDao.getByType("articles")
        if (stored?.hash == currentHash) return

        val cid = certId ?: serverUrlManager.getSelectedCertId()
        val response = syncApi.getArticles(certId = cid)
        val now = System.currentTimeMillis()

        articleDao.deleteAll()
        val articles = response.articles.map { a ->
            ArticleEntity(
                id = a.id,
                title = a.title,
                url = a.url,
                domainId = a.domainId,
                html = null,
                cachedAt = null
            )
        }
        articleDao.upsertAll(articles)

        // Fetch article HTML content in background (up to 3 concurrent)
        coroutineScope {
            val semaphore = Semaphore(3)
            articles.map { article ->
                async {
                    semaphore.withPermit {
                        try {
                            val content = syncApi.getArticleContent(article.id, certId = cid)
                            if (content.success) {
                                val updated = article.copy(
                                    html = content.data.html,
                                    cachedAt = System.currentTimeMillis()
                                )
                                articleDao.upsert(updated)
                            }
                        } catch (_: Exception) {
                            // Individual article fetch failure is non-fatal
                        }
                    }
                }
            }
        }

        syncMetadataDao.upsert(SyncMetadataEntity("articles", now, currentHash))
    }

    private suspend fun uploadLocalProgress(profileId: String): SyncResult {
        // Gather unsynced sessions
        val unsyncedSessions = quizSessionDao.getUnsynced()
        val unsyncedChapters = chapterProgressDao.getUnsynced()

        if (unsyncedSessions.isEmpty() && unsyncedChapters.isEmpty()) return SyncResult(true)

        val sessions = unsyncedSessions.map { session ->
            val attempts = questionAttemptDao.getBySession(session.clientId)
            UploadSessionDto(
                clientId = session.clientId,
                type = session.type,
                domainId = session.domainId,
                totalQuestions = session.totalQuestions,
                answeredQuestions = session.answeredQuestions,
                score = session.score,
                startedAt = session.startedAt,
                completedAt = session.completedAt,
                attempts = attempts.map { a ->
                    UploadAttemptDto(
                        questionId = a.questionId,
                        selectedAnswerIndex = a.selectedAnswerIndex,
                        isCorrect = a.isCorrect,
                        timeSpentSeconds = a.timeSpentSeconds
                    )
                }
            )
        }

        val chapterUpdates = unsyncedChapters.map { c ->
            UploadChapterDto(
                chapterId = c.chapterId,
                completedAt = c.completedAt,
                timeSpentSeconds = c.timeSpentSeconds
            )
        }

        val response = syncApi.uploadProgress(
            ProgressUploadRequest(
                profileId = profileId,
                sessions = sessions,
                chapterUpdates = chapterUpdates
            )
        )

        // Mark as synced
        unsyncedSessions.forEach { session ->
            quizSessionDao.markSynced(session.clientId, null)
        }
        unsyncedChapters.forEach { chapter ->
            chapterProgressDao.markSynced(chapter.chapterId)
        }

        return SyncResult(
            success = true,
            sessionsUploaded = response.processed.sessions,
            chaptersUploaded = response.processed.chapters
        )
    }

    private suspend fun downloadRemoteProgress(profileId: String) {
        val since = serverUrlManager.getLastProgressSyncTimestamp()
        val response = syncApi.downloadProgress(profileId, since = if (since > 0) since else null)

        // Import downloaded sessions (dedup by clientId)
        response.sessions.forEach { s ->
            val existing = s.clientId?.let { quizSessionDao.getByClientId(it) }
            if (existing == null && s.clientId != null) {
                quizSessionDao.upsert(
                    com.certforge.app.data.local.entity.QuizSessionEntity(
                        clientId = s.clientId,
                        serverId = s.id,
                        type = s.type,
                        domainId = s.domainId,
                        totalQuestions = s.totalQuestions,
                        answeredQuestions = s.answeredQuestions,
                        score = s.score,
                        startedAt = s.startedAt,
                        completedAt = s.completedAt,
                        isSynced = true
                    )
                )
            }
        }

        // Import chapter progress
        response.chapterProgress.forEach { c ->
            chapterProgressDao.upsert(
                com.certforge.app.data.local.entity.ChapterProgressEntity(
                    chapterId = c.chapterId,
                    completedAt = c.completedAt,
                    timeSpentSeconds = c.timeSpentSeconds,
                    isSynced = true
                )
            )
        }

        serverUrlManager.setLastProgressSyncTimestamp(response.serverTimestamp)
    }
}
