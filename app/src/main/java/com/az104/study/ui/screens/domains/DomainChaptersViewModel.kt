package com.az104.study.ui.screens.domains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.ChapterDao
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.DomainDao
import com.az104.study.data.local.entity.ChapterEntity
import com.az104.study.data.local.entity.DomainEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapterWithProgress(
    val chapter: ChapterEntity,
    val completedAt: Long?,
    val timeSpentSeconds: Int
)

@HiltViewModel
class DomainChaptersViewModel @Inject constructor(
    private val domainDao: DomainDao,
    private val chapterDao: ChapterDao,
    private val chapterProgressDao: ChapterProgressDao
) : ViewModel() {

    private val _domain = MutableStateFlow<DomainEntity?>(null)
    val domain: StateFlow<DomainEntity?> = _domain.asStateFlow()

    private val _chapters = MutableStateFlow<List<ChapterWithProgress>>(emptyList())
    val chapters: StateFlow<List<ChapterWithProgress>> = _chapters.asStateFlow()

    fun toggleChapterComplete(chapterId: String, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            chapterProgressDao.upsert(
                com.az104.study.data.local.entity.ChapterProgressEntity(
                    chapterId = chapterId,
                    completedAt = if (currentlyCompleted) null else System.currentTimeMillis(),
                    timeSpentSeconds = 0,
                    isSynced = false
                )
            )
        }
    }

    fun loadDomain(domainId: String) {
        viewModelScope.launch {
            _domain.value = domainDao.getById(domainId)
            combine(
                chapterDao.observeByDomain(domainId),
                chapterProgressDao.observeAll()
            ) { chapterList, progressList ->
                chapterList.map { chapter ->
                    val progress = progressList.find { it.chapterId == chapter.id }
                    ChapterWithProgress(
                        chapter = chapter,
                        completedAt = progress?.completedAt,
                        timeSpentSeconds = progress?.timeSpentSeconds ?: 0
                    )
                }
            }.collect { _chapters.value = it }
        }
    }
}
