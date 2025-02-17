package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.BottomNavItem

@Composable
fun HomeScreen(parentNavController: NavHostController) {
    // Create a separate NavController for bottom navigation.
    val bottomNavController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 4.dp) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                // Pass the parent NavController to DashboardScreen.
                DashboardScreen(parentNavController)
            }
            composable(BottomNavItem.Analytics.route) {
                AnalyticsScreen(parentNavController)
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(parentNavController)
            }
        }
    }
}
