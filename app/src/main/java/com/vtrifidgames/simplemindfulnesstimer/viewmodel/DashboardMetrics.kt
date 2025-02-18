package com.vtrifidgames.simplemindfulnesstimer.viewmodel

data class DashboardMetrics(
    val streak: Int = 0,            // Total distinct days meditated
    val lastWeekDuration: Long = 0, // in seconds
    val totalDuration: Long = 0     // in seconds
)

