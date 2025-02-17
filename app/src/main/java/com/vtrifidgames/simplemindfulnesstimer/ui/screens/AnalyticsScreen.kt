package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.AnalyticsViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.AnalyticsViewModelFactory

@Composable
fun AnalyticsScreen(navController: NavController) {
    // Get the database and create the repository.
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())

    // Obtain the AnalyticsViewModel using our custom factory.
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(repository)
    )
    val analyticsState by viewModel.analyticsState.collectAsState()

    // Display basic analytics.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Analytics Screen", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Total Sessions: ${analyticsState.totalSessions}")
        Text(text = "Average Duration: ${"%.2f".format(analyticsState.averageDuration)} sec")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

