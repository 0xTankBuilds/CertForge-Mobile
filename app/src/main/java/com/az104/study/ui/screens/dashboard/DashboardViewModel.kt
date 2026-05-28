package com.az104.study.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.ChapterDao
import com.az104.study.data.local.dao.ChapterProgressDao
import com.az104.study.data.local.dao.DomainDao
import com.az104.study.data.local.dao.QuestionAttemptDao
import com.az104.study.data.local.dao.QuestionDao
import com.az104.study.data.local.dao.QuizSessionDao
import com.az104.study.data.local.entity.QuizSessionEntity
import com.az104.study.util.DarkModePreference
import com.az104.study.util.TokenManager
import com.az104.study.util.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardStats(
    val chaptersCompleted: Int = 0,
    val totalChapters: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val correctRate: Int = 0,
    val totalQuestions: Int = 0,
    val streakDays: Int = 0,
    val recentSessions: List<QuizSessionEntity> = emptyList(),
    val domainNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val achievements: List<AchievementProgress> = emptyList(),
    val earnedAchievements: Int = 0,
    val totalAchievements: Int = 15
)

data class AchievementProgress(
    val id: String,
    val title: String,
    val iconSymbol: String,
    val description: String,
    val current: Int,
    val target: Int,
    val earned: Boolean
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    userPreferencesManager: UserPreferencesManager,
    private val chapterProgressDao: ChapterProgressDao,
    private val chapterDao: ChapterDao,
    private val questionAttemptDao: QuestionAttemptDao,
    private val quizSessionDao: QuizSessionDao,
    private val questionDao: QuestionDao,
    private val domainDao: DomainDao
) : ViewModel() {

    val isPaired: StateFlow<Boolean> = MutableStateFlow(tokenManager.isPaired())
    val darkMode: StateFlow<DarkModePreference> = userPreferencesManager.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DarkModePreference.SYSTEM)

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init { loadStats() }

    fun refreshPairingStatus() {
        (isPaired as MutableStateFlow).value = tokenManager.isPaired()
    }

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = DashboardStats(isLoading = true)

            val chaptersCompleted = chapterProgressDao.countCompleted()
            val totalChapters = chapterDao.count()
            val questionsAnswered = questionAttemptDao.count()
            val correctAnswers = questionAttemptDao.countCorrect()
            val totalQuestions = questionDao.getAll().size
            val correctRate = if (questionsAnswered > 0) (correctAnswers * 100) / questionsAnswered else 0
            val completionDates = chapterProgressDao.getCompletionDates()
            val streakDays = calculateStreak(completionDates.mapNotNull { parseDate(it) })
            val recentSessions = quizSessionDao.getRecent(5).filter { it.completedAt != null }
            val domains = domainDao.getAll().associate { it.id to it.name }

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

            val completedSessions = recentSessions

            val achievements = listOf(
                AchievementProgress("first_chapter", "First Steps", "👊", "Complete your first chapter", chaptersCompleted, 1, chaptersCompleted >= 1),
                AchievementProgress("five_chapters", "Bookworm", "📚", "Complete 5 chapters", chaptersCompleted, 5, chaptersCompleted >= 5),
                AchievementProgress("ten_chapters", "Scholar", "🎓", "Complete 10 chapters", chaptersCompleted, 10, chaptersCompleted >= 10),
                AchievementProgress("first_quiz", "Quiz Rookie", "💡", "Complete your first quiz", completedSessions.size, 1, completedSessions.isNotEmpty()),
                AchievementProgress("perfect_score", "Quiz Master", "🌟", "Score 100% on any quiz", completedSessions.count { it.score == 100 }, 1, completedSessions.any { it.score == 100 }),
                AchievementProgress("five_quizzes", "Quiz Addict", "📊", "Complete 5 quizzes", completedSessions.size, 5, completedSessions.size >= 5),
                AchievementProgress("perfect_3", "Perfect Hat Trick", "🏆", "Score 100% on 3 quizzes", completedSessions.count { it.score == 100 }, 3, completedSessions.count { it.score == 100 } >= 3),
                AchievementProgress("streak_7", "Steady Learner", "🔥", "Study 7 days in a row", streakDays.coerceAtMost(7), 7, isStreak(7)),
                AchievementProgress("streak_30", "Persistence", "💪", "Study 30 days in a row", streakDays.coerceAtMost(30), 30, isStreak(30)),
                AchievementProgress("domain_master", "Domain Expert", "🎯", "Complete all chapters in a domain", chaptersCompleted, 1, chaptersCompleted >= 1),
                AchievementProgress("century", "Century", "💯", "Answer 100 questions", questionsAnswered.coerceAtMost(100), 100, questionsAnswered >= 100),
                AchievementProgress("250_answers", "Dedicated", "📧", "Answer 250 questions", questionsAnswered.coerceAtMost(250), 250, questionsAnswered >= 250),
                AchievementProgress("high_accuracy", "Sharpshooter", "🏹", "Maintain 80%+ correct rate", correctRate, 80, questionsAnswered >= 10 && correctRate >= 80),
                AchievementProgress("speed_demon", "Speed Demon", "⚡", "Complete a chapter in under 5 min", 0, 1, false),
                AchievementProgress("all_domains", "Azure Expert", "🏆", "Complete all chapters in every domain", 0, 1, false)
            )
            val earnedCount = achievements.count { it.earned }

            _stats.value = DashboardStats(
                chaptersCompleted = chaptersCompleted,
                totalChapters = totalChapters,
                questionsAnswered = questionsAnswered,
                correctAnswers = correctAnswers,
                correctRate = correctRate,
                totalQuestions = totalQuestions,
                streakDays = streakDays,
                recentSessions = recentSessions,
                domainNames = domains,
                achievements = achievements,
                earnedAchievements = earnedCount,
                totalAchievements = achievements.size,
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
