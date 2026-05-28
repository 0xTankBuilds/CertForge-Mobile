package com.az104.study.ui.screens.domains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.DomainDao
import com.az104.study.data.local.entity.DomainEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DomainWithProgress(
    val domain: DomainEntity,
    val totalChapters: Int,
    val completedChapters: Int
)

@HiltViewModel
class DomainsViewModel @Inject constructor(
    private val domainDao: DomainDao,
    private val chapterProgressDao: ChapterProgressDao
) : ViewModel() {

    private val _domains = MutableStateFlow<List<DomainWithProgress>>(emptyList())
    val domains: StateFlow<List<DomainWithProgress>> = _domains.asStateFlow()

    init {
        viewModelScope.launch {
            domainDao.observeAll().collect { domainList ->
                val progress = chapterProgressDao.observeAll().first()
                _domains.value = domainList.map { domain ->
                    val totalChapters = estimateChapterCount(domain.id)
                    val completedChapters = progress.count {
                        it.chapterId.startsWith(domain.id) && it.completedAt != null
                    }
                    DomainWithProgress(domain, totalChapters, completedChapters)
                }
            }
        }
    }

    private fun estimateChapterCount(domainId: String): Int {
        return when (domainId) {
            "42" -> 3
            "5" -> 5
            "56" -> 5
            "16" -> 5
            "18" -> 4
            "35" -> 4
            else -> 0
        }
    }
}
