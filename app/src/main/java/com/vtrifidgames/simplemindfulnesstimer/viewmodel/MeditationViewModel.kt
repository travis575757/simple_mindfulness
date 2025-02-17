package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeditationViewModel(private val repository: MeditationRepository) : ViewModel() {

    // Expose all sessions as a StateFlow to be observed by the UI.
    val sessions: StateFlow<List<MeditationSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    // Add a new meditation session.
    fun addSession(session: MeditationSession) {
        viewModelScope.launch {
            repository.insert(session)
        }
    }

    // Remove a session using the complete session object.
    fun removeSession(session: MeditationSession) {
        viewModelScope.launch {
            repository.delete(session)
        }
    }

    // Remove a session by its id.
    fun removeSessionById(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteById(sessionId)
        }
    }
}

