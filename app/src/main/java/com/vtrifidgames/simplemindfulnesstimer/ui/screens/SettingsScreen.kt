package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SettingsViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SettingsViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import java.util.Locale
import com.vtrifidgames.simplemindfulnesstimer.ui.components.DurationPickerDialog

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    // Use the application context.
    val context = LocalContext.current.applicationContext
    val dataStore = remember { SettingsDataStore(context) }
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(dataStore))

    // Collect flows.
    val bellInterval by viewModel.bellInterval.collectAsState()
    val defaultTimerDuration by viewModel.defaultTimerDuration.collectAsState()
    val useBell by viewModel.useBell.collectAsState()

    // Local states for durations (in seconds).
    var localBellInterval by remember { mutableStateOf(bellInterval) }
    var localMeditationDuration by remember { mutableStateOf(defaultTimerDuration) }

    // Format seconds into MM:SS.
    fun formatDuration(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }

    // Update local state when flows update.
    LaunchedEffect(bellInterval) { localBellInterval = bellInterval }
    LaunchedEffect(defaultTimerDuration) { localMeditationDuration = defaultTimerDuration }

    // Local flags for showing the picker dialogs.
    var showBellPicker by remember { mutableStateOf(false) }
    var showMeditationPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // "Use Bell" Switch.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal=4.dp, vertical=0.dp)
        ) {
            Text(text = "Use Bell", modifier = Modifier.weight(1f))
            Switch(
                checked = useBell,
                onCheckedChange = { viewModel.updateUseBell(it) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Bell Interval field: clickable and read-only.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatDuration(localBellInterval),
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
                        showBellPicker = true
                    }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Meditation Duration field: clickable and read-only.
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatDuration(localMeditationDuration),
                onValueChange = {},
                readOnly = true,
                label = { Text("Meditation Duration (MM:SS)") },
                modifier = Modifier.fillMaxWidth(),
                colors =  OutlinedTextFieldDefaults.colors(
                    disabledTextColor = LocalContentColor.current,
                    disabledLabelColor = LocalContentColor.current.copy(alpha = 0.5f),
                    disabledBorderColor = LocalContentColor.current.copy(alpha = 0.5f)
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        showMeditationPicker = true
                    }
            )
        }
    }

    if (showBellPicker) {
        DurationPickerDialog(
            initialMinutes = (localBellInterval / 60).toInt(),
            initialSeconds = (localBellInterval % 60).toInt(),
            onDismiss = { showBellPicker = false },
            onConfirm = { newDuration ->
                localBellInterval = newDuration
                viewModel.updateBellInterval(newDuration)
                showBellPicker = false
            }
        )
    }

    if (showMeditationPicker) {
        DurationPickerDialog(
            initialMinutes = (localMeditationDuration / 60).toInt(),
            initialSeconds = (localMeditationDuration % 60).toInt(),
            onDismiss = { showMeditationPicker = false },
            onConfirm = { newDuration ->
                localMeditationDuration = newDuration
                viewModel.updateDefaultTimerDuration(newDuration)
                showMeditationPicker = false
            }
        )
    }
}
