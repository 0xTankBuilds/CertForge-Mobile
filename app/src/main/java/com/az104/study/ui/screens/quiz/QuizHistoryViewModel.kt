package com.az104.study.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.entity.QuizSessionEntity
import com.az104.study.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizHistoryState(
    val sessions: List<QuizSessionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class QuizHistoryViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizHistoryState())
    val state: StateFlow<QuizHistoryState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = QuizHistoryState(isLoading = true)
            val sessions = quizRepository.getSessions()
            _state.value = QuizHistoryState(sessions = sessions, isLoading = false)
        }
    }
}
