package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SettingsViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SettingsViewModelFactory
import androidx.compose.runtime.collectAsState

@Composable
fun SettingsScreen(navController: NavController) {
    // Use the application context to ensure persistence.
    val context = LocalContext.current.applicationContext
    val dataStore = remember { SettingsDataStore(context) }

    // Obtain the ViewModel using our custom factory.
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(dataStore)
    )

    // Collect current settings values.
    val bellInterval by viewModel.bellInterval.collectAsState()
    val defaultTimerDuration by viewModel.defaultTimerDuration.collectAsState()

    // Local state for text fields.
    var bellIntervalText by remember { mutableStateOf(bellInterval.toString()) }
    var defaultTimerDurationText by remember { mutableStateOf(defaultTimerDuration.toString()) }

    // Update local text states when the underlying DataStore values change.
    LaunchedEffect(bellInterval) {
        bellIntervalText = bellInterval.toString()
    }
    LaunchedEffect(defaultTimerDuration) {
        defaultTimerDurationText = defaultTimerDuration.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings", modifier = Modifier.padding(bottom = 16.dp))

        // Input field for Bell Interval.
        OutlinedTextField(
            value = bellIntervalText,
            onValueChange = { bellIntervalText = it },
            label = { Text("Bell Interval (seconds)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input field for Default Timer Duration.
        OutlinedTextField(
            value = defaultTimerDurationText,
            onValueChange = { defaultTimerDurationText = it },
            label = { Text("Default Timer Duration (seconds)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save button to update the settings.
        Button(onClick = {
            // Convert and update if valid.
            bellIntervalText.toLongOrNull()?.let { newInterval ->
                viewModel.updateBellInterval(newInterval)
            }
            defaultTimerDurationText.toLongOrNull()?.let { newDuration ->
                viewModel.updateDefaultTimerDuration(newDuration)
            }
        }) {
            Text("Save Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back button to return to the previous screen.
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
