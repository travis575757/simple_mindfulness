package com.vtrifidgames.simplemindfulnesstimer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : BottomNavItem("dashboard", Icons.Filled.Home, "Dashboard")
    object Analytics : BottomNavItem("analytics", Icons.Filled.DateRange, "Analytics")
    object Settings : BottomNavItem("settings", Icons.Filled.Settings, "Settings")
}
