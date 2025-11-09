package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModelFactory
import androidx.compose.runtime.collectAsState
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.ui.components.StarRatingSelector
import com.vtrifidgames.simplemindfulnesstimer.ui.components.ratingToStars
import com.vtrifidgames.simplemindfulnesstimer.ui.components.starsToRating
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(navController: NavController) {
    // Retrieve sessionId from navigation arguments.
    val navBackStackEntry = navController.currentBackStackEntry!!
    val sessionId = navBackStackEntry.arguments?.getLong("sessionId") ?: 0L

    // Set up repository and ViewModel.
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(repository, sessionId)
    )

    // Observe the session details.
    val session by viewModel.session.collectAsState()

    // Local UI state for rating and notes editing.
    var selectedStars by remember { mutableStateOf(0) }
    var showNoteEditor by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(session) {
        session?.let {
            selectedStars = ratingToStars(it.rating)
            if (!showNoteEditor) {
                noteText = it.notes.orEmpty()
            }
        }
    }

    // Formatter for displaying dates.
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            val currentSession = session
            if (currentSession == null) {
                // Show a progress indicator while loading.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.outlinedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header with Session ID.
                        Text(
                            text = "Session #${currentSession.id}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Divider()

                        // Row for Finish Date and Start Time.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Finish Date",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = dateFormat.format(Date(currentSession.date)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Start Time",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = dateFormat.format(Date(currentSession.time)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        // Row for Total Duration and Meditated Duration.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Duration",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${currentSession.durationTotal} sec",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Meditated Duration",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${currentSession.durationMeditated} sec",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        // Pauses.
                        Text(
                            text = "Pauses: ${currentSession.pauses}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Editable rating row.
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Rating",
                                style = MaterialTheme.typography.titleSmall
                            )
                            StarRatingSelector(
                                selectedStars = selectedStars,
                                onStarSelected = { star ->
                                    selectedStars = star
                                    viewModel.updateRating(starsToRating(star))
                                },
                                iconSize = 28.dp,
                                iconPadding = 2.dp
                            )
                        }

                        Divider()

                        // Notes section.
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (showNoteEditor) {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 80.dp),
                                    placeholder = { Text("Add details about this session") }
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(
                                        onClick = {
                                            showNoteEditor = false
                                            noteText = currentSession.notes.orEmpty()
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.updateNotes(noteText)
                                            showNoteEditor = false
                                        }
                                    ) {
                                        Text("Save")
                                    }
                                }
                            } else {
                                if (currentSession.notes.isNullOrBlank()) {
                                    Text(
                                        text = "No note recorded",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = currentSession.notes.orEmpty(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Button(
                                    onClick = {
                                        noteText = currentSession.notes.orEmpty()
                                        showNoteEditor = true
                                    },
                                    modifier = Modifier.align(Alignment.Start)
                                ) {
                                    Text(if (currentSession.notes.isNullOrBlank()) "Add Note" else "Edit Note")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
