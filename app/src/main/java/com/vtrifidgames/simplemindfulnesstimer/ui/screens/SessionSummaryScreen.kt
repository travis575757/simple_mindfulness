package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.ui.components.StarRatingSelector
import com.vtrifidgames.simplemindfulnesstimer.ui.components.ratingToStars
import com.vtrifidgames.simplemindfulnesstimer.ui.components.starsToRating
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
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: SessionSummaryViewModel = viewModel(
        factory = SessionSummaryViewModelFactory(repository, sessionId)
    )

    // Observe the session details.
    val session by viewModel.session.collectAsState()

    // Local state for star rating (1..5). Zero means not selected yet.
    var selectedStars by remember { mutableStateOf(0) }

    // When session loads, set the local stars to match the session’s rating.
    LaunchedEffect(session) {
        session?.let {
            selectedStars = ratingToStars(it.rating)
        }
    }

    // Local states for note editing.
    var showNoteEditor by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    // State for the delete confirmation dialog.
    var showDeleteDialog by remember { mutableStateOf(false) }

    // More human-readable date/time format.
    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,  // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1) Session Details in a card.
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Session Summary",
                    style = MaterialTheme.typography.headlineMedium
                )
                val currentSession = session
                if (currentSession == null) {
                    Text(
                        text = "Loading session details...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "Finish Date: ${dateFormat.format(Date(currentSession.date))}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Start Time: ${dateFormat.format(Date(currentSession.time))}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Total Duration: ${currentSession.durationTotal} sec",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Meditated Duration: ${currentSession.durationMeditated} sec",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Pauses: ${currentSession.pauses}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2) Star Rating
        Text(text = "Rate Your Session", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        StarRatingSelector(
            selectedStars = selectedStars,
            onStarSelected = { star ->
                selectedStars = star
                viewModel.updateRating(starsToRating(star))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3) Note Section
        val currentSession = session
        if (currentSession != null) {
            if (!currentSession.notes.isNullOrBlank() && !showNoteEditor) {
                // Display the current note with an Edit Note button.
                Text(
                    text = "Note: ${currentSession.notes}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    noteText = currentSession.notes.orEmpty()
                    showNoteEditor = true
                }) {
                    Text("Edit Note")
                }
            } else {
                if (!showNoteEditor) {
                    Button(onClick = {
                        noteText = currentSession.notes.orEmpty()
                        showNoteEditor = true
                    }) {
                        Text("Add Note")
                    }
                }
                if (showNoteEditor) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Enter note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showNoteEditor = false
                                noteText = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.updateNotes(noteText)
                                showNoteEditor = false
                                noteText = ""
                            }
                        ) {
                            Text("Save Note")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4) Action Buttons: Done and Discard Session
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { navController.navigate(Screen.Home.route) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Done")
            }
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Discard Session"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Discard")
            }
        }
    }

    // Delete confirmation dialog.
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Discard Session") },
            text = { Text("Are you sure you want to discard this session? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession()
                        showDeleteDialog = false
                        navController.navigate(Screen.Home.route)
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
