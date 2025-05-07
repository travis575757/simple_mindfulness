package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
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
import com.vtrifidgames.simplemindfulnesstimer.ui.components.DurationPickerDialog
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer
import com.vtrifidgames.simplemindfulnesstimer.utils.AlarmPlayer
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MainTimerViewModelFactory
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.TimerStatus
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(viewModel.navigateToSummary) {
        viewModel.navigateToSummary.collectLatest { sessionId ->
            navController.navigate("session_summary/$sessionId")
        }
    }

    // Local state for the user-selected duration (in seconds)
    var userSelectedDuration by remember { mutableStateOf(defaultTimerDuration) }
    // Update local state whenever defaultTimerDuration changes.
    LaunchedEffect(defaultTimerDuration) {
        userSelectedDuration = defaultTimerDuration
    }

    var showDurationPicker by remember { mutableStateOf(false) }

    // Pre-start display: format userSelectedDuration as MM:SS.
    val preMinutes = userSelectedDuration / 60
    val preSeconds = userSelectedDuration % 60
    val preDisplayTime = String.format(Locale.getDefault(), "%02d:%02d", preMinutes, preSeconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState.timerStatus) {
            TimerStatus.STOPPED -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = preDisplayTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bell Interval (MM:SS)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors =  OutlinedTextFieldDefaults.colors(
                            disabledTextColor = LocalContentColor.current,
                            disabledLabelColor = LocalContentColor.current.copy(alpha = 0.5f),
                            disabledBorderColor = LocalContentColor.current.copy(alpha = 0.5f)
                        )
                    )
                    // Overlay an invisible Box that intercepts taps.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                showDurationPicker = true
                            }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Set total duration and start timer.
                    viewModel.setTotalDuration(userSelectedDuration)
                    viewModel.setIntervalBell(uiState.intervalBellEnabled, defaultBellInterval)
                    viewModel.startTimer()
                }) {
                    Text("Start")
                }
            }
            TimerStatus.RUNNING, TimerStatus.PAUSED -> {
                // Running state: display time left in MM:SS format.
                val minutesLeft = uiState.timeLeft / 60
                val secondsLeft = uiState.timeLeft % 60
                val runningDisplayTime = String.format(Locale.getDefault(), "%02d:%02d", minutesLeft, secondsLeft)
                Text(
                    text = runningDisplayTime,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.timerStatus == TimerStatus.RUNNING) {
                        Button(onClick = { viewModel.pauseTimer() }) {
                            Text("Pause")
                        }
                    } else {
                        Button(onClick = { viewModel.resumeTimer() }) {
                            Text("Resume")
                        }
                    }
                    Button(onClick = { viewModel.finishSession() }) {
                        Text("Finish")
                    }
                    Button(onClick = { viewModel.addMinute() }) { Text("+1 min") }
                    Button(onClick = {
                        // On cancel, navigate to Home immediately.
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }) {
                        Text("Cancel")
                    }
                }
            }
            TimerStatus.FINISHED -> {
                Text(
                    text = "Time is up!",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.finishSession() }) {
                    Text("Finish")
                }
            }
        }
    }

    if (showDurationPicker) {
        DurationPickerDialog(
            initialMinutes = (userSelectedDuration / 60).toInt(),
            initialSeconds = (userSelectedDuration % 60).toInt(),
            onDismiss = { showDurationPicker = false },
            onConfirm = { newDuration ->
                userSelectedDuration = newDuration
                showDurationPicker = false
            }
        )
    }
}

