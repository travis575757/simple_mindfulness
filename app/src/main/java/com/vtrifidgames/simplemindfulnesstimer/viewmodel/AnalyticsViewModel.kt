package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AnalyticsState(
    val totalSessions: Int = 0,
    val averageDuration: Double = 0.0
)

class AnalyticsViewModel(private val repository: MeditationRepository) : ViewModel() {

    // Calculate analytics based on the sessions list from the repository.
    val analyticsState: StateFlow<AnalyticsState> = repository.allSessions
        .map { sessions ->
            val total = sessions.size
            val avg = if (total > 0) sessions.sumOf { it.durationMeditated } / total.toDouble() else 0.0
            AnalyticsState(total, avg)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, AnalyticsState())

}

