package com.az104.study.ui.screens.studyguides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.ChapterDao
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.StudyGuideDao
import com.az104.study.data.local.entity.ChapterProgressEntity
import com.az104.study.data.local.entity.StudyGuideEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyGuideState(
    val guide: StudyGuideEntity? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isCompleted: Boolean = false,
    val isOffline: Boolean = false
)

@HiltViewModel
class StudyGuideViewModel @Inject constructor(
    private val studyGuideDao: StudyGuideDao,
    private val chapterDao: ChapterDao,
    private val chapterProgressDao: ChapterProgressDao
) : ViewModel() {

    private val _state = MutableStateFlow(StudyGuideState())
    val state: StateFlow<StudyGuideState> = _state.asStateFlow()
    private var articleId: String = ""

    fun loadGuide(articleId: String) {
        this.articleId = articleId
        viewModelScope.launch {
            _state.value = StudyGuideState(isLoading = true)
            val guide = studyGuideDao.getByArticleId(articleId)
            val chapter = chapterDao.getByArticleId(articleId)
            val progress = chapter?.let { chapterProgressDao.getByChapterId(it.id) }
            val isOffline = guide != null
            if (guide != null) {
                _state.value = StudyGuideState(
                    guide = guide,
                    isLoading = false,
                    isCompleted = progress?.completedAt != null,
                    isOffline = isOffline
                )
            } else {
                _state.value = StudyGuideState(
                    isLoading = false,
                    notFound = true,
                    isOffline = isOffline
                )
            }
        }
    }

    fun markComplete() {
        viewModelScope.launch {
            try {
                val chapter = chapterDao.getByArticleId(articleId) ?: return@launch
                val currentlyCompleted = _state.value.isCompleted
                chapterProgressDao.upsert(
                    ChapterProgressEntity(
                        chapterId = chapter.id,
                        completedAt = if (currentlyCompleted) null else System.currentTimeMillis(),
                        timeSpentSeconds = 0,
                        isSynced = false
                    )
                )
                _state.value = _state.value.copy(isCompleted = !currentlyCompleted)
            } catch (_: Exception) {
                // silently ignore
            }
        }
    }
}
