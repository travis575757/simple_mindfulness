package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.ui.navigation.Screen
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModelFactory

@Composable
fun SessionHistoryScreen(navController: NavController) {
    // Retrieve the context and set up the database and repository.
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())

    // Obtain the ViewModel (which exposes a Flow of sessions).
    val viewModel: MeditationViewModel = viewModel(
        factory = MeditationViewModelFactory(repository)
    )
    val sessions by viewModel.sessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Session History",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Use LazyColumn to display the list of sessions.
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(sessions) { session ->
                SessionHistoryItem(
                    session = session,
                    onItemClick = {
                        // Navigate to SessionDetailScreen using the session's ID.
                        val sessionId = session.id;
                        navController.navigate("session_detail/$sessionId")
                    },
                    onDelete = { viewModel.removeSession(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

@Composable
fun SessionHistoryItem(
    session: MeditationSession,
    onItemClick: () -> Unit,
    onDelete: (MeditationSession) -> Unit
) {
    // Wrap the session row in a clickable modifier.
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
            Text(text = "Duration: ${session.duration} sec")
            Text(text = "Date: ${session.date}")
        }
        Button(onClick = { onDelete(session) }) {
            Text("Delete")
        }
    }
}
