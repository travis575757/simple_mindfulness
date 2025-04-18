package com.vtrifidgames.simplemindfulnesstimer.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object MainTimer : Screen("main_timer")
    object Settings : Screen("settings")
    object Analytics : Screen("analytics")
    object SessionHistory : Screen("session_history")
    object SessionDetail : Screen("session_detail/{sessionId}")
    object SessionSummary : Screen("session_summary/{sessionId}")
}
