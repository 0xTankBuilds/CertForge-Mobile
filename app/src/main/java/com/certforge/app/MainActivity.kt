package com.certforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.certforge.app.ui.navigation.BottomNavItem
import com.certforge.app.ui.navigation.NavGraph
import com.certforge.app.ui.screens.dashboard.DashboardViewModel
import com.certforge.app.ui.theme.CertForgeTheme
import com.certforge.app.util.DarkModePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val darkMode by dashboardViewModel.darkMode.collectAsStateWithLifecycle()
            val isPaired by dashboardViewModel.isPaired.collectAsStateWithLifecycle()

            CertForgeTheme(
                darkModePreference = darkMode
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Bottom bar visibility: hide on scan/pairing screens
                val showBottomBar = currentRoute != null &&
                    currentRoute !in listOf(
                        "scan-qr", "pairing", "article/{articleId}",
                        "study-guide/{articleId}", "quiz/session/{domainId}",
                        "quiz/results/{sessionClientId}", "quiz/history"
                    )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                BottomNavItem.entries.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        label = { Text(item.label) },
                                        icon = {
                                            val imageVector = when (item) {
                                                BottomNavItem.DASHBOARD -> Icons.Default.Home
                                                BottomNavItem.STUDY -> Icons.AutoMirrored.Filled.MenuBook
                                                BottomNavItem.QUIZ -> Icons.Default.Quiz
                                                BottomNavItem.ANALYTICS -> Icons.Default.Analytics
                                                BottomNavItem.SETTINGS -> Icons.Default.Settings
                                            }
                                            Icon(imageVector, contentDescription = item.label)
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph(
                            navController = navController,
                            isPaired = isPaired,
                            onNavigateToPairing = {
                                navController.navigate("scan-qr") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
