package com.vtrifidgames.simplemindfulnesstimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.AppNavHost
import com.vtrifidgames.simplemindfulnesstimer.ui.theme.SimpleMindfulnessTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the app layout edge-to-edge
        enableEdgeToEdge()

        setContent {
            SimpleMindfulnessTimerTheme {
                // Create the NavController
                val navController = rememberNavController()
                // Host the NavGraph
                AppNavHost(navController = navController)
            }
        }
    }
}