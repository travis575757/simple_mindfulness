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

import androidx.navigation.NavType
import androidx.navigation.navArgument

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
        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            SessionDetailScreen(navController)
        }
        composable(
            route = Screen.SessionSummary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            SessionSummaryScreen(navController)
        }
    }
}
