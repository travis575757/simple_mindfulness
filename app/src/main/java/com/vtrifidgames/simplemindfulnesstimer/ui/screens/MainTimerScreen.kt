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
import androidx.compose.runtime.collectAsState
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer
import com.vtrifidgames.simplemindfulnesstimer.utils.AlarmPlayer
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModelFactory
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.TimerStatus
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainTimerScreen(navController: NavController) {
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())

    val appContext = LocalContext.current.applicationContext
    val settingsDataStore = remember { SettingsDataStore(appContext) }
    val defaultTimerDuration by settingsDataStore.defaultTimerDurationFlow.collectAsState(initial = 300L)
    val defaultBellInterval by settingsDataStore.bellIntervalFlow.collectAsState(initial = 60L)

    val bellPlayer = remember { BellPlayer(appContext) }
    val alarmPlayer = remember { AlarmPlayer(appContext) }

    val viewModel: MainTimerViewModel = viewModel(
        factory = MainTimerViewModelFactory(repository, settingsDataStore, bellPlayer, alarmPlayer)
    )

    val uiState by viewModel.uiState.collectAsState()

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

        // If timer is finished, show the Finish button; else show normal controls.
        if (uiState.timerStatus == TimerStatus.FINISHED) {
            Button(onClick = { viewModel.finishSession() }) {
                Text("Finish")
            }
        } else {
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
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
