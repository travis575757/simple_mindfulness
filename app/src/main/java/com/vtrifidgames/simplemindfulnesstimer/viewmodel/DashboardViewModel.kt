package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardViewModel(private val repository: MeditationRepository) : ViewModel() {
    val metrics: StateFlow<DashboardMetrics> = repository.allSessions
        .map { sessions ->
            // Compute the "streak" as the number of distinct days on which sessions occurred.
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val distinctDays = sessions.map { dateFormat.format(Date(it.date)) }.toSet().size

            val now = System.currentTimeMillis()
            val weekMillis = TimeUnit.DAYS.toMillis(7)
            // Total meditated time in the last week (using durationMeditated, in seconds)
            val lastWeekDuration = sessions
                .filter { it.date >= now - weekMillis }
                .sumOf { it.durationMeditated }
            // Overall total meditated time (in seconds)
            val totalDuration = sessions.sumOf { it.durationMeditated }

            DashboardMetrics(
                streak = distinctDays,
                lastWeekDuration = lastWeekDuration,
                totalDuration = totalDuration
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, DashboardMetrics())
}

