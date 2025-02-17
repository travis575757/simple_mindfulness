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
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.DashboardScreen
import com.vtrifidgames.simplemindfulnesstimer.ui.screens.HomeScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            // HomeScreen will display the bottom navigation and manage its own inner NavHost.
            HomeScreen(parentNavController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
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
