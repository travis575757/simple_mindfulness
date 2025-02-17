package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen

@Composable
fun SettingsScreen(navController: NavController) {
    Text(text = "Settings Screen")
    Button(onClick = { navController.popBackStack() }) {
        Text("Back")
    }
}

