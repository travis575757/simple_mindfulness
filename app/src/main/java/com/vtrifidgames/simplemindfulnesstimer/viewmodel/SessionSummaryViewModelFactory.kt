package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository

class SessionSummaryViewModelFactory(
    private val repository: MeditationRepository,
    private val sessionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionSummaryViewModel(repository, sessionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

