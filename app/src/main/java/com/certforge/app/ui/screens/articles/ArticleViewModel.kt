package com.certforge.app.ui.screens.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certforge.app.data.local.dao.ArticleDao
import com.certforge.app.data.local.dao.ChapterDao
import com.certforge.app.data.local.dao.ChapterProgressDao
import com.certforge.app.data.local.dao.StudyGuideDao
import com.certforge.app.data.local.entity.ArticleEntity
import com.certforge.app.data.local.entity.ChapterProgressEntity
import com.certforge.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleState(
    val article: ArticleEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasStudyGuide: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val articleDao: ArticleDao,
    private val studyGuideDao: StudyGuideDao,
    private val chapterDao: ChapterDao,
    private val chapterProgressDao: ChapterProgressDao
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleState())
    val state: StateFlow<ArticleState> = _state.asStateFlow()

    private var startTimeMillis = 0L

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            try {
                _state.value = ArticleState(isLoading = true)
                startTimeMillis = System.currentTimeMillis()

                val chapter = chapterDao.getByArticleId(articleId)
                val existingProgress = chapter?.let { chapterProgressDao.getByChapterId(it.id) }
                val isCompleted = existingProgress?.completedAt != null

                // Try cache first
                val cached = articleDao.getById(articleId)
                if (cached?.html != null) {
                    val hasGuide = studyGuideDao.getByArticleId(articleId) != null
                    _state.value = ArticleState(
                        article = cached,
                        isLoading = false,
                        hasStudyGuide = hasGuide,
                        isCompleted = isCompleted
                    )
                    return@launch
                }

                // Fetch from server
                val article = contentRepository.fetchArticleContent(articleId)
                val hasGuide = studyGuideDao.getByArticleId(articleId) != null
                _state.value = ArticleState(
                    article = article,
                    isLoading = false,
                    hasStudyGuide = hasGuide,
                    isCompleted = isCompleted
                )
            } catch (e: Throwable) {
                _state.value = ArticleState(
                    isLoading = false,
                    error = "Failed to load article: ${e.message}"
                )
            }
        }
    }

    fun markComplete() {
        val article = _state.value.article ?: return
        viewModelScope.launch {
            try {
                val chapter = chapterDao.getByArticleId(article.id) ?: return@launch
                val elapsed = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
                chapterProgressDao.upsert(
                    ChapterProgressEntity(
                        chapterId = chapter.id,
                        completedAt = System.currentTimeMillis(),
                        timeSpentSeconds = elapsed.coerceAtLeast(0),
                        isSynced = false
                    )
                )
                _state.value = _state.value.copy(isCompleted = true)
            } catch (_: Exception) {
                // silently ignore — not critical
            }
        }
    }
}
