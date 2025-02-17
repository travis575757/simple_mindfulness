package com.vtrifidgames.simplemindfulnesstimer.ui.screens

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
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.MeditationViewModelFactory

@Composable
fun SessionHistoryScreen(navController: NavController) {
    // Retrieve the context
    val context = LocalContext.current
    // Get the singleton instance of the database
    val database = MeditationDatabase.getDatabase(context)
    // Create a repository using the DAO from the database
    val repository = MeditationRepository(database.meditationSessionDao())

    // Obtain the ViewModel using the custom factory that requires the repository.
    val viewModel: MeditationViewModel = viewModel(
        factory = MeditationViewModelFactory(repository)
    )

    // Observe sessions as state from the ViewModel.
    val sessions by viewModel.sessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)
    ) {
        Text(
            text = "Session History",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of sessions using LazyColumn.
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(sessions) { session ->
                SessionItem(session = session, onDelete = {
                    viewModel.removeSession(it)
                })
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
fun SessionItem(session: MeditationSession, onDelete: (MeditationSession) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
