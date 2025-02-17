package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SettingsViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SettingsViewModelFactory
import androidx.compose.runtime.collectAsState

@Composable
fun SettingsScreen(navController: NavController) {
    // Use the application context.
    val context = LocalContext.current.applicationContext
    val dataStore = remember { SettingsDataStore(context) }
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(dataStore))
    val bellInterval by viewModel.bellInterval.collectAsState()
    val defaultTimerDuration by viewModel.defaultTimerDuration.collectAsState()

    var bellIntervalText by remember { mutableStateOf(bellInterval.toString()) }
    var defaultTimerDurationText by remember { mutableStateOf(defaultTimerDuration.toString()) }

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
        OutlinedTextField(
            value = bellIntervalText,
            onValueChange = { bellIntervalText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Bell Interval (seconds)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = defaultTimerDurationText,
            onValueChange = { defaultTimerDurationText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Default Timer Duration (seconds)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            bellIntervalText.toLongOrNull()?.let { viewModel.updateBellInterval(it) }
            defaultTimerDurationText.toLongOrNull()?.let { viewModel.updateDefaultTimerDuration(it) }
        }) {
            Text("Save Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
