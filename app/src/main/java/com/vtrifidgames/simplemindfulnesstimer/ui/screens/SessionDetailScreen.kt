package com.vtrifidgames.simplemindfulnesstimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModelFactory
import androidx.compose.runtime.collectAsState

@Composable
fun SessionDetailScreen(navController: NavController) {
    // Retrieve the current back stack entry and extract the sessionId argument.
    val navBackStackEntry: NavBackStackEntry = navController.currentBackStackEntry!!
    val sessionId = navBackStackEntry.arguments?.getLong("sessionId") ?: 0L

    // Set up the Repository and ViewModel.
    val context = LocalContext.current
    val database = MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(repository, sessionId)
    )

    // Collect the session details from the ViewModel.
    val session by viewModel.session.collectAsState()

    // Use a local variable for a safe smart cast.
    val currentSession = session

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
         verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Session Details", modifier = Modifier.padding(bottom = 16.dp))
        if (currentSession == null) {
            Text(text = "Loading session details...")
        } else {
            Text(text = "Session ID: ${currentSession.id}")
            Text(text = "Date: ${currentSession.date}")
            Text(text = "Duration: ${currentSession.duration} sec")
            if (!currentSession.notes.isNullOrEmpty()) {
                Text(text = "Note: ${currentSession.notes}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

