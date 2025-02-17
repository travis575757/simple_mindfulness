package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen

@Composable
fun DashboardScreen(navController: NavController) {
    // Dashboard with one button to navigate to the Main Timer.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { navController.navigate(Screen.MainTimer.route) }) {
            Text("Go to Timer")
        }
    }
}
