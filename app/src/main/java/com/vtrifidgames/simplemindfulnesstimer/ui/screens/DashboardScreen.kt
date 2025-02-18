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
            modifier = Modifier.padding(bottom = 24.dp).align(Alignment.CenterHorizontally)
        )
        // Card containing the metrics.
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Only display a metric if its value is nonzero.
                if (metrics.streak > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Streak", style = MaterialTheme.typography.titleMedium)
                        Text(text = "${metrics.streak} days", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (metrics.lastWeekDuration > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Last Week", style = MaterialTheme.typography.titleMedium)
                        Text(text = formatDurationHuman(metrics.lastWeekDuration), style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (metrics.totalDuration > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total", style = MaterialTheme.typography.titleMedium)
                        Text(text = formatDurationHuman(metrics.totalDuration), style = MaterialTheme.typography.bodyLarge)
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
