package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModelFactory
import androidx.compose.runtime.collectAsState
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating.*
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
            if (session == null) {
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
                            text = "Session #${session!!.id}",
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
                                    text = dateFormat.format(Date(session!!.date)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Start Time",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = dateFormat.format(Date(session!!.time)),
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
                                    text = "${session!!.durationTotal} sec",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Meditated Duration",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${session!!.durationMeditated} sec",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        // Pauses.
                        Text(
                            text = "Pauses: ${session!!.pauses}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Rating with stars.
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rating: ",
                                style = MaterialTheme.typography.titleSmall
                            )
                            val stars = when (session!!.rating) {
                                VERY_POOR -> 1
                                POOR -> 2
                                AVERAGE -> 3
                                GOOD -> 4
                                EXCELLENT -> 5
                            }
                            Row {
                                for (i in 1..5) {
                                    val tint = if (i <= stars)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Optional note.
                        session!!.notes?.takeIf { it.isNotBlank() }?.let { note ->
                            Column {
                                Text(
                                    text = "Note",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

