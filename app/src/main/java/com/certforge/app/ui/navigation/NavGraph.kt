package com.certforge.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.certforge.app.ui.screens.achievements.AchievementsScreen
import com.certforge.app.ui.screens.analytics.AnalyticsScreen
import com.certforge.app.ui.screens.articles.ArticleScreen
import com.certforge.app.ui.screens.dashboard.DashboardScreen
import com.certforge.app.ui.screens.domains.DomainChaptersScreen
import com.certforge.app.ui.screens.domains.DomainsScreen
import com.certforge.app.ui.screens.pairing.PairingSetupScreen
import com.certforge.app.ui.screens.quiz.QuizHistoryScreen
import com.certforge.app.ui.screens.quiz.QuizResultsScreen
import com.certforge.app.ui.screens.quiz.QuizSelectScreen
import com.certforge.app.ui.screens.quiz.QuizSessionScreen
import com.certforge.app.ui.screens.scanning.ScanQrScreen
import com.certforge.app.ui.screens.settings.SettingsScreen
import com.certforge.app.ui.screens.studyguides.StudyGuideScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    isPaired: Boolean,
    onNavigateToPairing: () -> Unit
) {
    val startDestination = if (isPaired) Route.Dashboard.route else Route.ScanQr.create()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Dashboard.route) {
            DashboardScreen(
                onNavigateToDomains = { navController.navigate(Route.Domains.route) },
                onNavigateToQuiz = { navController.navigate(Route.QuizSelect.route) }
            )
        }

        composable(Route.Domains.route) {
            DomainsScreen(
                onDomainClick = { domainId ->
                    navController.navigate(Route.DomainChapters.create(domainId))
                }
            )
        }

        composable(
            route = Route.DomainChapters.route,
            arguments = listOf(navArgument("domainId") { type = NavType.StringType })
        ) { backStackEntry ->
            val domainId = backStackEntry.arguments?.getString("domainId") ?: return@composable
            DomainChaptersScreen(
                domainId = domainId,
                onArticleClick = { articleId ->
                    navController.navigate(Route.Article.create(articleId))
                },
                onStudyGuideClick = { articleId ->
                    navController.navigate(Route.StudyGuide.create(articleId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Route.Article.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleScreen(
                articleId = articleId,
                onBackClick = { navController.popBackStack() },
                onStudyGuideClick = {
                    navController.navigate(Route.StudyGuide.create(articleId))
                }
            )
        }

        composable(
            route = Route.StudyGuide.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            StudyGuideScreen(
                articleId = articleId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Route.QuizSelect.route) {
            QuizSelectScreen(
                onStartQuiz = { domainId ->
                    navController.navigate(Route.QuizSession.create(domainId))
                },
                onHistoryClick = {
                    navController.navigate(Route.QuizHistory.route)
                }
            )
        }

        composable(
            route = Route.QuizSession.route,
            arguments = listOf(navArgument("domainId") { type = NavType.StringType })
        ) { backStackEntry ->
            val domainId = backStackEntry.arguments?.getString("domainId")
            QuizSessionScreen(
                domainId = domainId.takeIf { it != "all" },
                onQuizComplete = { clientId ->
                    navController.navigate(Route.QuizResults.create(clientId)) {
                        popUpTo(Route.QuizSelect.route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Route.QuizResults.route,
            arguments = listOf(navArgument("sessionClientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("sessionClientId") ?: return@composable
            QuizResultsScreen(
                sessionClientId = clientId,
                onBackToQuizSelect = {
                    navController.navigate(Route.QuizSelect.route) {
                        popUpTo(Route.Dashboard.route)
                    }
                },
                onRetryIncorrect = {
                    navController.navigate(Route.QuizSession.create(null))
                }
            )
        }

        composable(Route.QuizHistory.route) {
            QuizHistoryScreen(
                onSessionClick = { clientId ->
                    navController.navigate(Route.QuizResults.create(clientId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Route.Analytics.route) {
            AnalyticsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Route.Achievements.route) {
            AchievementsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Route.Settings.route) {
            SettingsScreen(
                onScanQr = { navController.navigate(Route.ScanQr.create()) },
                onNavigateToDashboard = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Route.ScanQr.route,
            arguments = emptyList()
        ) {
            ScanQrScreen(
                onPairingComplete = { setupToken ->
                    navController.navigate(
                        Route.Pairing.create(setupToken)
                    ) {
                        popUpTo(Route.ScanQr.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Route.Pairing.route,
            arguments = listOf(
                navArgument("setupToken") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setupToken = backStackEntry.arguments?.getString("setupToken") ?: return@composable

            PairingSetupScreen(
                setupToken = setupToken,
                onComplete = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onError = {
                    navController.navigate(Route.ScanQr.create()) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
