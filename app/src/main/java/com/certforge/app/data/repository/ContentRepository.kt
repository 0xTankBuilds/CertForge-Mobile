package com.certforge.app.data.repository

import com.certforge.app.data.local.dao.ArticleDao
import com.certforge.app.data.local.dao.ChapterDao
import com.certforge.app.data.local.dao.DomainDao
import com.certforge.app.data.local.dao.StudyGuideDao
import com.certforge.app.data.local.entity.ArticleEntity
import com.certforge.app.data.local.entity.ChapterEntity
import com.certforge.app.data.local.entity.DomainEntity
import com.certforge.app.data.local.entity.StudyGuideEntity
import com.certforge.app.data.remote.SyncApi
import com.certforge.app.util.ServerUrlManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContentRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val articleDao: ArticleDao,
    private val studyGuideDao: StudyGuideDao,
    private val domainDao: DomainDao,
    private val chapterDao: ChapterDao,
    private val serverUrlManager: ServerUrlManager
) {
    fun observeDomains(): Flow<List<DomainEntity>> = domainDao.observeAll()

    fun observeChaptersByDomain(domainId: String): Flow<List<ChapterEntity>> =
        chapterDao.observeByDomain(domainId)

    fun observeArticles(): Flow<List<ArticleEntity>> = articleDao.observeAll()

    suspend fun getChapterById(id: String): ChapterEntity? = chapterDao.getById(id)

    suspend fun getArticleById(id: String): ArticleEntity? = articleDao.getById(id)

    suspend fun getDomainById(id: String): DomainEntity? = domainDao.getById(id)

    suspend fun getStudyGuide(articleId: String): StudyGuideEntity? =
        studyGuideDao.getByArticleId(articleId)

    /**
     * Fetch article HTML from the server. Updates the local cache.
     * Implements LRU eviction: keeps last 10 cached articles.
     */
    suspend fun fetchArticleContent(articleId: String): ArticleEntity {
        val cached = articleDao.getById(articleId)
        if (cached?.html != null) return cached

        val certId = serverUrlManager.getSelectedCertId()
        val response = syncApi.getArticleContent(articleId, certId = certId)
        if (!response.success) {
            throw RuntimeException("Server returned error for article $articleId")
        }

        val now = System.currentTimeMillis()

        // LRU eviction: keep last 10 cached
        val cachedArticles = articleDao.getCachedArticles()
        if (cachedArticles.size >= 10) {
            val toEvict = cachedArticles.take(cachedArticles.size - 9)
            toEvict.forEach { articleDao.deleteById(it.id) }
        }

        val updated = if (cached != null) {
            cached.copy(html = response.data.html, cachedAt = now)
        } else {
            val domainId = chapterDao.getByArticleId(articleId)?.domainId ?: ""
            ArticleEntity(
                id = articleId,
                title = response.data.title,
                url = "",
                domainId = domainId,
                html = response.data.html,
                cachedAt = now
            )
        }
        articleDao.upsert(updated)
        return updated
    }
}
