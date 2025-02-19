package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionSummaryViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionSummaryViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.alpha
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Convert a rating enum to the number of stars (1..5).
private fun ratingToStars(rating: Rating): Int = when (rating) {
    Rating.VERY_POOR -> 1
    Rating.POOR -> 2
    Rating.AVERAGE -> 3
    Rating.GOOD -> 4
    Rating.EXCELLENT -> 5
}

// Convert a number of stars (1..5) to a rating enum.
private fun starsToRating(stars: Int): Rating = when (stars) {
    1 -> Rating.VERY_POOR
    2 -> Rating.POOR
    3 -> Rating.AVERAGE
    4 -> Rating.GOOD
    5 -> Rating.EXCELLENT
    else -> Rating.AVERAGE
}

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
    var showRemoveNoteDialog by remember { mutableStateOf(false) }

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
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (star in 1..5) {
                val icon = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.Star
                Icon(
                    imageVector = icon,
                    contentDescription = "Star $star",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .alpha(if (star > selectedStars) 0.25f else 1.0f)
                        .clickable {
                            selectedStars = star
                            viewModel.updateRating(starsToRating(star))
                        },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3) Note Section
        val currentSession = session
        if (currentSession != null) {
            if (!currentSession.notes.isNullOrBlank() && !showNoteEditor) {
                // Display the current note with a Remove Note button.
                Text(
                    text = "Note: ${currentSession.notes}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showRemoveNoteDialog = true }) {
                    Text("Remove Note")
                }
            } else {
                if (!showNoteEditor) {
                    Button(onClick = { showNoteEditor = true }) {
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
                    Button(
                        onClick = {
                            showNoteEditor = false
                            noteText = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        if (showRemoveNoteDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveNoteDialog = false },
                title = { Text("Confirm Removal") },
                text = { Text("Are you sure you want to remove the note?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateNotes("")
                        showRemoveNoteDialog = false
                    }) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveNoteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4) Done Button - note submission is handled here
        Button(
            onClick = {
                if (showNoteEditor && noteText.isNotBlank()) {
                    viewModel.updateNotes(noteText)
                }
                navController.navigate(Screen.Home.route)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedStars > 0
        ) {
            Text("Done")
        }
    }
}

