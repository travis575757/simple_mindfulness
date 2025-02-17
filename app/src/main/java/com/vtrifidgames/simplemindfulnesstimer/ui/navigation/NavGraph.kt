package com.vtrifidgames.simplemindfulnesstimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.AnalyticsScreen
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.MainTimerScreen
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.SessionDetailScreen
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.SessionHistoryScreen
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.SessionSummaryScreen
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainTimer.route
    ) {
        composable(Screen.MainTimer.route) {
            MainTimerScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController)
        }
        composable(Screen.SessionHistory.route) {
            SessionHistoryScreen(navController)
        }
        composable(Screen.SessionDetail.route) {
            SessionDetailScreen(navController)
        }
        composable(Screen.SessionSummary.route) {
            SessionSummaryScreen(navController)
        }
    }
}
