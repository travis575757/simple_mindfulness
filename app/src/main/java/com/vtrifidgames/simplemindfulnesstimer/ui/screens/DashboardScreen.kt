package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.DashboardViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.DashboardViewModelFactory
import java.util.Locale

@Composable
fun DashboardScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(repository))
    val metrics by viewModel.metrics.collectAsState()

    // Helper function to format durations in a human-friendly way.
    fun formatDurationHuman(seconds: Long): String {
        return when {
            seconds == 0L -> ""
            seconds < 60 -> "$seconds seconds"
            seconds < 3600 -> {
                val minutes = seconds / 60
                val remainder = seconds % 60
                if (remainder < 15) "$minutes minutes"
                else String.format(Locale.getDefault(), "%.1f minutes", minutes + remainder / 60.0)
            }
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                String.format(Locale.getDefault(), "%.1f hours", hours + minutes / 60.0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large welcome message.
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Simple\nMindfulness",
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Cursive,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.CenterHorizontally)
        )
        // Build a list of metric items to display only if the value is non-zero.
        val metricsList = mutableListOf<Pair<String, String>>()
        if (metrics.streak > 0) {
            metricsList.add("Streak" to "${metrics.streak} day(s)")
        }
        if (metrics.lastWeekDuration > 0) {
            metricsList.add("Last Week" to formatDurationHuman(metrics.lastWeekDuration))
        }
        if (metrics.totalDuration > 0) {
            metricsList.add("Total" to formatDurationHuman(metrics.totalDuration))
        }
        // Only display the card if we have any metric to show.
        if (metricsList.isNotEmpty()) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    metricsList.forEachIndexed { index, metric ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = metric.first, style = MaterialTheme.typography.titleMedium)
                            Text(text = metric.second, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (index != metricsList.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        // Start Meditation button.
        Button(
            onClick = { navController.navigate(Screen.MainTimer.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Meditation")
        }
    }
}
