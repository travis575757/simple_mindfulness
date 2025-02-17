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
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModel
import com.vtrifidgames.simplemindfulnesstimer.viewmodel.SessionDetailViewModelFactory
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(navController: NavController) {
    // Retrieve sessionId from navigation arguments.
    val navBackStackEntry: NavBackStackEntry = navController.currentBackStackEntry!!
    val sessionId = navBackStackEntry.arguments?.getLong("sessionId") ?: 0L

    // Set up repository and ViewModel.
    val context = LocalContext.current
    val database = com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationDatabase.getDatabase(context)
    val repository = MeditationRepository(database.meditationSessionDao())
    val viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(repository, sessionId)
    )

    val session by viewModel.session.collectAsState()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Session Details", modifier = Modifier.padding(bottom = 16.dp))

        var currentSession = session;
        if (currentSession == null) {
            Text("Loading session details...")
        } else {
            Text(text = "Session ID: ${currentSession.id}")
            Text(text = "Finish Date: ${dateFormat.format(Date(currentSession.date))}")
            Text(text = "Start Time: ${dateFormat.format(Date(currentSession.time))}")
            Text(text = "Total Duration: ${currentSession.durationTotal} sec")
            Text(text = "Meditated Duration: ${currentSession.durationMeditated} sec")
            Text(text = "Pauses: ${currentSession.pauses}")
            Text(text = "Rating: ${currentSession.rating.name}")
            currentSession.notes?.let {
                if (it.isNotEmpty()) {
                    Text(text = "Note: $it")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
