package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun SessionDetailScreen(navController: NavController) {
    Text(text = "Session Detail Screen")
    Button(onClick = { navController.popBackStack() }) {
        Text("Back")
    }
}

