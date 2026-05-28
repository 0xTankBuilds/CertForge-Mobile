package com.az104.study.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.repository.QuizRepository
import com.az104.study.data.repository.QuizResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizResultsState(
    val result: QuizResult? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

@HiltViewModel
class QuizResultsViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizResultsState())
    val state: StateFlow<QuizResultsState> = _state.asStateFlow()

    fun loadResults(sessionClientId: String) {
        viewModelScope.launch {
            _state.value = QuizResultsState(isLoading = true)
            val result = quizRepository.getResult(sessionClientId)
            if (result != null) {
                _state.value = QuizResultsState(result = result, isLoading = false)
            } else {
                _state.value = QuizResultsState(isLoading = false, notFound = true)
            }
        }
    }
}
