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
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModelFactory

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Session ID: ${session.id}")
            Text(text = "Duration: ${session.durationMeditated} sec")
            Text(text = "Date: ${session.date}")
        }
        Button(onClick = { onDelete(session) }) {
            Text("Delete")
        }
    }
}
