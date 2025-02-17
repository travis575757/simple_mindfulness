package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModelFactory
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.TimerStatus
import kotlinx.coroutines.flow.collectAsState

@Composable
fun MainTimerScreen(navController: NavController) {
    // Get DB and Repository
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())

    // Obtain the ViewModel
    val viewModel: MainTimerViewModel = viewModel(
        factory = MainTimerViewModelFactory(repository)
    )

    // Observe the UI state
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the time left
        Text(text = "Time Left: ${uiState.timeLeft} sec")

        Spacer(modifier = Modifier.height(16.dp))

        // Simple row of timer control buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.startTimer() }) {
                Text("Start")
            }
            Button(onClick = { viewModel.pauseTimer() }) {
                Text("Pause")
            }
            Button(onClick = { viewModel.resumeTimer() }) {
                Text("Resume")
            }
            Button(onClick = { viewModel.resetTimer() }) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the current timer status
        Text(text = "Status: ${uiState.timerStatus}")

        // Example toggle for interval bells (optional)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.setIntervalBell(
                enabled = !uiState.intervalBellEnabled,
                intervalSeconds = 60L
            )
        }) {
            Text(text = if (uiState.intervalBellEnabled) "Disable Bells" else "Enable Bells")
        }
    }
}
