package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val repository: MeditationRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _session = MutableStateFlow<MeditationSession?>(null)
    val session: StateFlow<MeditationSession?> = _session.asStateFlow()

    init {
        viewModelScope.launch {
            _session.value = repository.getSessionById(sessionId)
        }
    }

    fun updateNotes(newNotes: String) {
        viewModelScope.launch {
            _session.value?.let { session ->
                val updatedSession = session.copy(notes = newNotes)
                repository.update(updatedSession)
                _session.value = updatedSession
            }
        }
    }

    fun updateRating(newRating: Rating) {
        viewModelScope.launch {
            _session.value?.let { session ->
                val updatedSession = session.copy(rating = newRating)
                repository.update(updatedSession)
                _session.value = updatedSession
            }
        }
    }
}
