// File: ./viewmodel/AnalyticsViewModel.kt
package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

enum class AnalyticsInterval {
    DAYS, WEEKS, MONTHS
}

class AnalyticsViewModel(private val repository: MeditationRepository) : ViewModel() {

    private val _selectedInterval = MutableStateFlow(AnalyticsInterval.DAYS)
    val selectedInterval: StateFlow<AnalyticsInterval> = _selectedInterval.asStateFlow()

    fun setSelectedInterval(interval: AnalyticsInterval) {
        _selectedInterval.value = interval
    }

    val analyticsData: StateFlow<AnalyticsData> =
        combine(repository.allSessions, _selectedInterval) { sessions, interval ->
            computeAnalyticsData(sessions, interval)
        }.stateIn(viewModelScope, SharingStarted.Lazily, AnalyticsData())

    private fun computeAnalyticsData(
        sessions: List<MeditationSession>,
        interval: AnalyticsInterval
    ): AnalyticsData {
        // Generate the list of periods and labels
        val (periods, labels) = generatePeriodKeysAndLabels(interval)

        // Map to hold the aggregated data per period
        val periodData = mutableMapOf<Long, MutableList<MeditationSession>>()
        for (session in sessions) {
            val periodKey = getSessionPeriodKey(session.date, interval)
            val list = periodData.getOrPut(periodKey) { mutableListOf() }
            list.add(session)
        }

        // Lists to hold the data arrays
        val totalMeditationTime = mutableListOf<Float>()
        val totalTime = mutableListOf<Float>()
        val sessionsCount = mutableListOf<Float>()
        val pauses = mutableListOf<Float>()
        val ratings = mutableListOf<Float>()

        for (period in periods) {
            val sessionsInPeriod = periodData[period] ?: emptyList()
            val totalMeditationTimeInPeriod = sessionsInPeriod.sumOf { it.durationMeditated }.toFloat()
            val totalTimeInPeriod = sessionsInPeriod.sumOf { it.durationTotal }.toFloat()
            val sessionsCountInPeriod = sessionsInPeriod.size.toFloat()
            val pausesInPeriod = sessionsInPeriod.sumOf { it.pauses }.toFloat()
            val averageRatingInPeriod = if (sessionsInPeriod.isNotEmpty()) {
                sessionsInPeriod.map { ratingToFloat(it.rating) }.average().toFloat()
            } else 0f

            totalMeditationTime.add(totalMeditationTimeInPeriod)
            totalTime.add(totalTimeInPeriod)
            sessionsCount.add(sessionsCountInPeriod)
            pauses.add(pausesInPeriod)
            ratings.add(averageRatingInPeriod)
        }

        return AnalyticsData(
            totalMeditationTime = totalMeditationTime,
            totalTime = totalTime,
            sessions = sessionsCount,
            pauses = pauses,
            rating = ratings,
            xAxisLabels = labels
        )
    }

    private fun generatePeriodKeysAndLabels(interval: AnalyticsInterval): Pair<List<Long>, List<String>> {
        val periods = mutableListOf<Long>()
        val labels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = when (interval) {
            AnalyticsInterval.DAYS -> SimpleDateFormat("EEE", Locale.getDefault()) // e.g., Mon, Tue
            AnalyticsInterval.WEEKS -> SimpleDateFormat("'Wk'w", Locale.getDefault()) // e.g., Wk1
            AnalyticsInterval.MONTHS -> SimpleDateFormat("MMM", Locale.getDefault()) // e.g., Jan
        }
        val numPeriods = when (interval) {
            AnalyticsInterval.DAYS -> 7
            AnalyticsInterval.WEEKS -> 4
            AnalyticsInterval.MONTHS -> 12
        }
        // Start from today
        calendar.timeInMillis = System.currentTimeMillis()
        // Reset time fields
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        for (i in (numPeriods - 1) downTo 0) {
            val periodCalendar = calendar.clone() as Calendar
            when (interval) {
                AnalyticsInterval.DAYS -> {
                    periodCalendar.add(Calendar.DAY_OF_YEAR, -i)
                    periodCalendar.set(Calendar.HOUR_OF_DAY, 0)
                }
                AnalyticsInterval.WEEKS -> {
                    periodCalendar.add(Calendar.WEEK_OF_YEAR, -i)
                    periodCalendar.set(Calendar.DAY_OF_WEEK, periodCalendar.firstDayOfWeek)
                    periodCalendar.set(Calendar.HOUR_OF_DAY, 0)
                }
                AnalyticsInterval.MONTHS -> {
                    periodCalendar.add(Calendar.MONTH, -i)
                    periodCalendar.set(Calendar.DAY_OF_MONTH, 1)
                    periodCalendar.set(Calendar.HOUR_OF_DAY, 0)
                }
            }
            periodCalendar.set(Calendar.MINUTE, 0)
            periodCalendar.set(Calendar.SECOND, 0)
            periodCalendar.set(Calendar.MILLISECOND, 0)
            val periodKey = periodCalendar.timeInMillis
            periods.add(periodKey)
            labels.add(dateFormat.format(periodCalendar.time))
        }

        return Pair(periods, labels)
    }

    private fun getSessionPeriodKey(sessionDate: Long, interval: AnalyticsInterval): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = sessionDate }
        when (interval) {
            AnalyticsInterval.DAYS -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
            }
            AnalyticsInterval.WEEKS -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
            }
            AnalyticsInterval.MONTHS -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
            }
        }
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun ratingToFloat(rating: Rating): Float {
        return when (rating) {
            Rating.VERY_POOR -> 1f
            Rating.POOR -> 2f
            Rating.AVERAGE -> 3f
            Rating.GOOD -> 4f
            Rating.EXCELLENT -> 5f
        }
    }
}

// Data class to hold the state data for the analytics
data class AnalyticsData(
    val totalMeditationTime: List<Float> = emptyList(),
    val totalTime: List<Float> = emptyList(),
    val sessions: List<Float> = emptyList(),
    val pauses: List<Float> = emptyList(),
    val rating: List<Float> = emptyList(),
    val xAxisLabels: List<String> = emptyList()
)