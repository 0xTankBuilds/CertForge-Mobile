package com.az104.study.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.entity.QuestionEntity
import com.az104.study.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizSessionState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val isAnswered: Boolean = false,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val sessionClientId: String? = null,
    val startTimeMillis: Long = 0L
) {
    val currentQuestion: QuestionEntity? get() = questions.getOrNull(currentIndex)
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentIndex >= questions.size - 1
}

@HiltViewModel
class QuizSessionViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizSessionState())
    val state: StateFlow<QuizSessionState> = _state.asStateFlow()

    private val _completedSessionId = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val completedSessionId: SharedFlow<String> = _completedSessionId.asSharedFlow()

    fun startQuiz(domainId: String?) {
        viewModelScope.launch {
            _state.value = QuizSessionState(isLoading = true)

            val questions = quizRepository.getQuestions(domainId)
            if (questions.isEmpty()) {
                _state.value = QuizSessionState(isLoading = false, isEmpty = true)
                return@launch
            }

            val session = quizRepository.createSession(
                type = if (domainId != null) "domain" else "comprehensive",
                domainId = domainId,
                totalQuestions = questions.size
            )

            _state.value = QuizSessionState(
                questions = questions,
                sessionClientId = session.clientId,
                isLoading = false,
                startTimeMillis = System.currentTimeMillis()
            )
        }
    }

    fun selectAnswer(index: Int) {
        val currentState = _state.value
        if (currentState.isAnswered) return

        _state.value = currentState.copy(
            selectedAnswer = index,
            isAnswered = true
        )
    }

    fun nextQuestion() {
        val currentState = _state.value
        val question = currentState.currentQuestion ?: return
        val selected = currentState.selectedAnswer ?: return

        viewModelScope.launch {
            val elapsed = ((System.currentTimeMillis() - currentState.startTimeMillis) / 1000).toInt()

            quizRepository.submitAnswer(
                sessionClientId = currentState.sessionClientId!!,
                questionId = question.id,
                selectedAnswerIndex = selected,
                timeSpentSeconds = elapsed.coerceAtLeast(0),
                isCorrect = selected == question.correctAnswerIndex
            )

            if (currentState.isLastQuestion) {
                _completedSessionId.emit(currentState.sessionClientId!!)
            } else {
                _state.value = _state.value.copy(
                    currentIndex = currentState.currentIndex + 1,
                    selectedAnswer = null,
                    isAnswered = false,
                    startTimeMillis = System.currentTimeMillis()
                )
            }
        }
    }
}
