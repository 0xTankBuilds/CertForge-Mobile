package com.az104.study.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.DomainDao
import com.az104.study.data.local.dao.QuestionDao
import com.az104.study.data.local.entity.DomainEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizSelectState(
    val domains: List<DomainWithCount> = emptyList(),
    val totalQuestions: Int = 0,
    val isLoading: Boolean = true
)

data class DomainWithCount(
    val domain: DomainEntity,
    val questionCount: Int
)

@HiltViewModel
class QuizSelectViewModel @Inject constructor(
    private val domainDao: DomainDao,
    private val questionDao: QuestionDao
) : ViewModel() {

    private val _state = MutableStateFlow(QuizSelectState())
    val state: StateFlow<QuizSelectState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = QuizSelectState(isLoading = true)
            val allQuestions = questionDao.getAll()
            val domains = domainDao.getAll()
            val domainCounts = domains.map { domain ->
                val count = allQuestions.count { it.domainId == domain.id }
                DomainWithCount(domain, count)
            }
            _state.value = QuizSelectState(
                domains = domainCounts,
                totalQuestions = allQuestions.size,
                isLoading = false
            )
        }
    }
}
