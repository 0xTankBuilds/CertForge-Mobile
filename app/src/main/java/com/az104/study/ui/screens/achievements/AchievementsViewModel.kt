package com.az104.study.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.QuestionAttemptDao
import com.az104.study.data.local.dao.QuizSessionDao
import com.az104.study.data.local.entity.QuizSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val isEarned: Boolean = false,
    val earnedDate: LocalDate? = null
)

data class AchievementsState(
    val achievements: List<Achievement> = emptyList(),
    val earnedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val chapterProgressDao: ChapterProgressDao,
    private val questionAttemptDao: QuestionAttemptDao,
    private val quizSessionDao: QuizSessionDao
) : ViewModel() {

    private val _state = MutableStateFlow(AchievementsState())
    val state: StateFlow<AchievementsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = AchievementsState(isLoading = true)

            val chaptersCompleted = chapterProgressDao.countCompleted()
            val questionsAnswered = questionAttemptDao.count()
            val correctAnswers = questionAttemptDao.countCorrect()
            val totalSessions = quizSessionDao.getRecent(100)
            val completedSessions = totalSessions.filter { it.completedAt != null }
            val completionDates = chapterProgressDao.getCompletionDates()
            val dates = completionDates.mapNotNull { parseDate(it) }.toSet()
            val now = LocalDate.now()

            fun isStreak(days: Int): Boolean {
                var d = now
                repeat(days) {
                    if (d !in dates) return false
                    d = d.minusDays(1)
                }
                return true
            }

            fun perfectScores(sessions: List<QuizSessionEntity>): Int =
                sessions.count { it.score == 100 }

            val achievements = listOf(
                Achievement("first_chapter", "First Steps", "Complete your first chapter", "👊",
                    isEarned = chaptersCompleted >= 1),
                Achievement("five_chapters", "Bookworm", "Complete 5 chapters", "📚",
                    isEarned = chaptersCompleted >= 5),
                Achievement("ten_chapters", "Scholar", "Complete 10 chapters", "🎓",
                    isEarned = chaptersCompleted >= 10),
                Achievement("first_quiz", "Quiz Rookie", "Complete your first quiz", "💡",
                    isEarned = completedSessions.isNotEmpty()),
                Achievement("perfect_score", "Quiz Master", "Score 100% on any quiz", "🌟",
                    isEarned = completedSessions.any { it.score == 100 }),
                Achievement("five_quizzes", "Quiz Addict", "Complete 5 quizzes", "📊",
                    isEarned = completedSessions.size >= 5),
                Achievement("perfect_3", "Perfect Hat Trick", "Score 100% on 3 quizzes", "🏆",
                    isEarned = perfectScores(completedSessions) >= 3),
                Achievement("streak_7", "Steady Learner", "Study 7 days in a row", "🔥",
                    isEarned = isStreak(7)),
                Achievement("streak_30", "Persistence", "Study 30 days in a row", "💪",
                    isEarned = isStreak(30)),
                Achievement("domain_master", "Domain Expert", "Complete all chapters in one domain", "🎯",
                    isEarned = chaptersCompleted >= 1),
                Achievement("century", "Century", "Answer 100 questions", "💯",
                    isEarned = questionsAnswered >= 100),
                Achievement("250_answers", "Dedicated", "Answer 250 questions", "📧",
                    isEarned = questionsAnswered >= 250),
                Achievement("high_accuracy", "Sharpshooter", "Maintain 80%+ correct rate", "🏹",
                    isEarned = questionsAnswered >= 10 && correctAnswers * 100 / questionsAnswered >= 80),
                Achievement("speed_demon", "Speed Demon", "Complete a chapter in under 5 min", "⚡",
                    isEarned = false),
                Achievement("all_domains", "Azure Expert", "Complete all chapters in every domain", "🏆",
                    isEarned = false)
            )

            val earnedCount = achievements.count { it.isEarned }

            _state.value = AchievementsState(
                achievements = achievements,
                earnedCount = earnedCount,
                totalCount = achievements.size,
                isLoading = false
            )
        }
    }

    private fun parseDate(dateStr: String): LocalDate? = try {
        LocalDate.parse(dateStr)
    } catch (_: Exception) { null }
}
