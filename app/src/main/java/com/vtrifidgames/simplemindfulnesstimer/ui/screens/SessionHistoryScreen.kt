package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(navController: NavController) {
    // Retrieve context and set up database and repository.
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())

    // Obtain the ViewModel.
    val viewModel: MeditationViewModel = viewModel(
        factory = MeditationViewModelFactory(repository)
    )
    val sessions = viewModel.sessions.collectAsState().value

    // Use Scaffold with a bottomBar that applies navigationBar insets.
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars, // Let content respect system insets.
        bottomBar = {
            // Apply navigation bar insets plus additional padding.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Session History",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { session ->
                        SessionHistoryItem(
                            session = session,
                            onItemClick = {
                                // Navigate to SessionDetailScreen using the session's ID.
                                navController.navigate("session_detail/${session.id}")
                            },
                            onDelete = { viewModel.removeSession(it) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SessionHistoryItem(
    session: MeditationSession,
    onItemClick: () -> Unit,
    onDelete: (MeditationSession) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(session)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Row for date and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                    Text(
                        text = dateFormatter.format(Date(session.date)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Display star rating
                    val stars = when (session.rating) {
                        Rating.VERY_POOR -> 1
                        Rating.POOR -> 2
                        Rating.AVERAGE -> 3
                        Rating.GOOD -> 4
                        Rating.EXCELLENT -> 5
                    }
                    Row {
                        for (i in 1..6) {
                            val tint = if (i <= stars)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Row for meditated and total time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meditated: ${session.durationMeditated} sec",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total: ${session.durationTotal} sec",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Row for pauses and truncated notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pauses: ${session.pauses}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val notesText = session.notes?.let {
                        if (it.length > 20) it.take(20) + "..."
                        else it
                    } ?: "No notes"
                    Text(
                        text = notesText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // Trash icon for delete
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete session"
                )
            }
        }
    }
}
