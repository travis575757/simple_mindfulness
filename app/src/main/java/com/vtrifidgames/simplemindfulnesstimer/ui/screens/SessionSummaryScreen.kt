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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionSummaryViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionSummaryViewModelFactory
import androidx.compose.runtime.collectAsState

@Composable
fun SessionSummaryScreen(navController: NavController) {
    // Retrieve the current back stack entry to extract the sessionId argument.
    val navBackStackEntry: NavBackStackEntry = navController.currentBackStackEntry!!
    val sessionId = navBackStackEntry.arguments?.getLong("sessionId") ?: 0L

    // Set up the Repository and ViewModel.
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: SessionSummaryViewModel = viewModel(
        factory = SessionSummaryViewModelFactory(repository, sessionId)
    )

    // Collect the session details.
    val session by viewModel.session.collectAsState()

    // Local state for the note text and to toggle its visibility.
    var notesText by remember { mutableStateOf("") }
    var showNoteField by remember { mutableStateOf(false) }

    // When the session loads, initialize notesText (and optionally show the note field if a note exists).
    LaunchedEffect(session) {
        session?.notes?.let {
            notesText = it
            if (it.isNotEmpty()) {
                showNoteField = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Session Summary", modifier = Modifier.padding(bottom = 16.dp))

        var currentSession = session;
        if (currentSession == null) {
            Text(text = "Loading session details...")
        } else {
            // Display session details.
            Text(text = "Session ID: ${currentSession.id}")
            Text(text = "Date: ${currentSession.date}")
            Text(text = "Duration: ${currentSession.duration} sec")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle button: "Add Note" when hidden, "Remove Note" when the note field is visible.
        Button(onClick = {
            showNoteField = !showNoteField
            if (!showNoteField) {
                // If toggled off, clear the note.
                notesText = ""
            }
        }) {
            Text(text = if (showNoteField) "Remove Note" else "Add Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the note text field if toggled visible.
        if (showNoteField) {
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The Done button saves the note (if non-empty) before navigating away.
        Button(onClick = {
            if (notesText.isNotEmpty()) {
                viewModel.updateNotes(notesText)
            }
            navController.navigate(Screen.MainTimer.route)
        }) {
            Text("Done")
        }
    }
}
