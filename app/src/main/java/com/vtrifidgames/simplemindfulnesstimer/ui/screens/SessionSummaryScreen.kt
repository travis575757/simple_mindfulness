package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionSummaryViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionSummaryViewModelFactory
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionSummaryScreen(navController: NavController) {
    // Retrieve the sessionId from navigation arguments.
    val navBackStackEntry = navController.currentBackStackEntry!!
    val sessionId = navBackStackEntry.arguments?.getLong("sessionId") ?: 0L

    // Set up repository and ViewModel.
    val context = LocalContext.current
    val database = com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: SessionSummaryViewModel = viewModel(
        factory = SessionSummaryViewModelFactory(repository, sessionId)
    )

    // Collect session details.
    val session by viewModel.session.collectAsState()

    // Local state for note text and to toggle its visibility.
    var notesText by remember { mutableStateOf("") }
    var showNoteField by remember { mutableStateOf(false) }
    // Local state for rating; initially null means not selected.
    var selectedRating by remember { mutableStateOf<Rating?>(null) }

    LaunchedEffect(session) {
        session?.notes?.let {
            notesText = it
            if (it.isNotEmpty()) {
                showNoteField = true
            }
        }
        // If the session already has a rating, pre-select it.
        session?.let {
            selectedRating = it.rating
        }
    }

    // Formatter for timestamps.
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Session Summary", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        var currentSession = session
        if (currentSession == null) {
            Text("Loading session details...")
        } else {
            Text(text = "Session ID: ${currentSession.id}")
            Text(text = "Finish Date: ${dateFormat.format(Date(currentSession.date))}")
            Text(text = "Start Time: ${dateFormat.format(Date(currentSession.time))}")
            Text(text = "Total Duration: ${currentSession.durationTotal} sec")
            Text(text = "Meditated Duration: ${currentSession.durationMeditated} sec")
            Text(text = "Pauses: ${currentSession.pauses}")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Rating selection row.
        Text(text = "Select Rating:", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Rating.values().forEach { ratingOption ->
                Button(
                    onClick = {
                        selectedRating = ratingOption
                        viewModel.updateRating(ratingOption)
                    },
                    // Highlight selected rating.
                    colors = if (selectedRating == ratingOption)
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    else androidx.compose.material3.ButtonDefaults.buttonColors()
                ) {
                    Text(text = ratingOption.name.replace("_", " "))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle button for notes.
        Button(onClick = {
            showNoteField = !showNoteField
            if (!showNoteField) {
                notesText = ""
            }
        }) {
            Text(text = if (showNoteField) "Remove Note" else "Add Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showNoteField) {
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // The Done button is enabled only if a rating is selected.
        Button(
            onClick = {
                if (notesText.isNotEmpty()) {
                    viewModel.updateNotes(notesText)
                }
                // Navigate to Main Timer screen.
                navController.navigate(Screen.MainTimer.route)
            },
            enabled = (selectedRating != null)
        ) {
            Text("Done")
        }
    }
}
