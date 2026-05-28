package com.az104.study.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.ChapterDao
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.DomainDao
import com.az104.study.data.local.dao.QuestionAttemptDao
import com.az104.study.data.local.dao.QuestionDao
import com.az104.study.data.local.dao.QuizSessionDao
import com.az104.study.data.local.entity.QuizSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DomainBreakdown(
    val domainName: String,
    val chaptersCompleted: Int,
    val totalChapters: Int,
    val progress: Float
)

data class AnalyticsState(
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val correctRate: Int = 0,
    val chaptersCompleted: Int = 0,
    val totalChapters: Int = 0,
    val totalSessions: Int = 0,
    val studyStreak: Int = 0,
    val recentSessions: List<QuizSessionEntity> = emptyList(),
    val domainBreakdown: List<DomainBreakdown> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val questionAttemptDao: QuestionAttemptDao,
    private val questionDao: QuestionDao,
    private val chapterProgressDao: ChapterProgressDao,
    private val chapterDao: ChapterDao,
    private val domainDao: DomainDao,
    private val quizSessionDao: QuizSessionDao
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = AnalyticsState(isLoading = true)

            val questionsAnswered = questionAttemptDao.count()
            val correctAnswers = questionAttemptDao.countCorrect()
            val correctRate = if (questionsAnswered > 0) (correctAnswers * 100) / questionsAnswered else 0
            val chaptersCompleted = chapterProgressDao.countCompleted()
            val totalChapters = chapterDao.count()
            val totalSessions = quizSessionDao.countCompleted()
            val completionDates = chapterProgressDao.getCompletionDates()
            val studyStreak = calculateStreak(completionDates.mapNotNull { parseDate(it) })
            val recentSessions = quizSessionDao.getRecent(10).filter { it.completedAt != null }

            // Per-domain breakdown
            val domains = domainDao.getAll()
            val allChapters = chapterDao.getAll()
            val domainBreakdown = domains.map { domain ->
                val domainChapters = allChapters.filter { it.domainId == domain.id }
                val completed = domainChapters.count { ch ->
                    chapterProgressDao.getByChapterId(ch.id)?.completedAt != null
                }
                DomainBreakdown(
                    domainName = domain.name,
                    chaptersCompleted = completed,
                    totalChapters = domainChapters.size,
                    progress = if (domainChapters.isNotEmpty())
                        completed.toFloat() / domainChapters.size else 0f
                )
            }

            _state.value = AnalyticsState(
                questionsAnswered = questionsAnswered,
                correctAnswers = correctAnswers,
                correctRate = correctRate,
                chaptersCompleted = chaptersCompleted,
                totalChapters = totalChapters,
                totalSessions = totalSessions,
                studyStreak = studyStreak,
                recentSessions = recentSessions,
                domainBreakdown = domainBreakdown,
                isLoading = false
            )
        }
    }

    private fun calculateStreak(dates: List<LocalDate>): Int {
        val dateSet = dates.toSet()
        var streak = 0
        var day = LocalDate.now()
        while (dateSet.contains(day)) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    private fun parseDate(dateStr: String): LocalDate? = try {
        LocalDate.parse(dateStr)
    } catch (_: Exception) { null }
}
