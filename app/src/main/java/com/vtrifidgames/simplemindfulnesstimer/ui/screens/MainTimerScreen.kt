package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import android.app.Activity
import android.view.WindowManager
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModelFactory
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.TimerStatus
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainTimerScreen(navController: NavController) {
    // Get the database and repository.
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())

    // Get settings using the application context.
    val appContext = LocalContext.current.applicationContext
    val settingsDataStore = remember { SettingsDataStore(appContext) }
    // Collect settings values.
    val defaultTimerDuration by settingsDataStore.defaultTimerDurationFlow.collectAsState(initial = 300L)
    val defaultBellInterval by settingsDataStore.bellIntervalFlow.collectAsState(initial = 60L)

    // Create a BellPlayer instance.
    val bellPlayer = remember { BellPlayer(appContext) }

    // Obtain the ViewModel using our updated factory.
    val viewModel: MainTimerViewModel = viewModel(
        factory = MainTimerViewModelFactory(repository, settingsDataStore, bellPlayer)
    )

    // Observe the timer UI state.
    val uiState by viewModel.uiState.collectAsState()

    // Prevent the screen from sleeping while the timer is running.
    val activity = LocalContext.current as Activity
    DisposableEffect(uiState.timerStatus) {
        if (uiState.timerStatus == TimerStatus.RUNNING) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Listen for navigation events.
    LaunchedEffect(key1 = viewModel.navigateToSummary) {
        viewModel.navigateToSummary.collectLatest { sessionId ->
            navController.navigate("session_summary/$sessionId")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Time Left: ${uiState.timeLeft} sec")
        Spacer(modifier = Modifier.height(16.dp))
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
        Text(text = "Status: ${uiState.timerStatus}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.setIntervalBell(
                enabled = !uiState.intervalBellEnabled,
                intervalSeconds = defaultBellInterval
            )
        }) {
            Text(text = if (uiState.intervalBellEnabled) "Disable Bells" else "Enable Bells")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Navigation")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate(Screen.Settings.route) }) {
                Text("Go to Settings")
            }
            Button(onClick = { navController.navigate(Screen.Analytics.route) }) {
                Text("View Analytics")
            }
            Button(onClick = { navController.navigate(Screen.SessionHistory.route) }) {
                Text("Session History")
            }
        }
    }
}
