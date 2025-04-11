package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardViewModel(private val repository: MeditationRepository) : ViewModel() {
    val metrics: StateFlow<DashboardMetrics> = repository.allSessions
        .map { sessions ->
            // Compute the current streak as the number of consecutive days meditated ending with today.
            fun dayStart(time: Long): Long {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = time
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return calendar.timeInMillis
            }

            // Get distinct days based on session dates
            val distinctDays = sessions.map { dayStart(it.date) }.toSet()
            val today = dayStart(System.currentTimeMillis())
            var streak = 0
            if (distinctDays.contains(today)) {
                streak = 1
                var currentDay = today
                while (true) {
                    currentDay -= TimeUnit.DAYS.toMillis(1)
                    if (distinctDays.contains(currentDay)) {
                        streak++
                    } else {
                        break
                    }
                }
            }

            val now = System.currentTimeMillis()
            val weekMillis = TimeUnit.DAYS.toMillis(7)
            // Total meditated time in the last week (using durationMeditated, in seconds)
            val lastWeekDuration = sessions
                .filter { it.date >= now - weekMillis }
                .sumOf { it.durationMeditated }
            // Overall total meditated time (in seconds)
            val totalDuration = sessions.sumOf { it.durationMeditated }

            DashboardMetrics(
                streak = streak,
                lastWeekDuration = lastWeekDuration,
                totalDuration = totalDuration
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, DashboardMetrics())
}
