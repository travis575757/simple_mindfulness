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
        val score = mutableListOf<Float>() // ← new list for score

        for (period in periods) {
            val sessionsInPeriod = periodData[period] ?: emptyList()
            val totalMeditationTimeInPeriod = sessionsInPeriod.sumOf { it.durationMeditated }.toFloat()
            val totalTimeInPeriod = sessionsInPeriod.sumOf { it.durationTotal }.toFloat()
            val sessionsCountInPeriod = sessionsInPeriod.size.toFloat()
            val pausesInPeriod = sessionsInPeriod.sumOf { it.pauses }.toFloat()
            val averageRatingInPeriod = if (sessionsInPeriod.isNotEmpty()) {
                sessionsInPeriod.map { ratingToFloat(it.rating) }.average().toFloat()
            } else 0f

            // ← compute score = (durationMeditated in minutes) × rating
            val scoreInPeriod = sessionsInPeriod.fold(0f) { acc, session ->
                acc + (session.durationMeditated.toFloat() / 60f) * ratingToFloat(session.rating)
            }

            totalMeditationTime.add(totalMeditationTimeInPeriod)
            totalTime.add(totalTimeInPeriod)
            sessionsCount.add(sessionsCountInPeriod)
            pauses.add(pausesInPeriod)
            ratings.add(averageRatingInPeriod)
            score.add(scoreInPeriod) // ← add to score list
        }

        return AnalyticsData(
            totalMeditationTime = totalMeditationTime,
            totalTime = totalTime,
            sessions = sessionsCount,
            pauses = pauses,
            rating = ratings,
            score = score,             // ← pass score into the data class
            xAxisLabels = labels
        )
    }

    private fun generatePeriodKeysAndLabels(interval: AnalyticsInterval): Pair<List<Long>, List<String>> {
        val periods = mutableListOf<Long>()
        val labels = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        val dateFormat = when (interval) {
            AnalyticsInterval.DAYS -> SimpleDateFormat("EEE", Locale.getDefault()) // e.g., Mon, Tue
            AnalyticsInterval.WEEKS -> SimpleDateFormat("dd/MM", Locale.getDefault()) // e.g., 05/03
            AnalyticsInterval.MONTHS -> SimpleDateFormat("MMM", Locale.getDefault())    // e.g., Jan
        }

        val numPeriods = when (interval) {
            AnalyticsInterval.DAYS -> 7
            AnalyticsInterval.WEEKS -> 4
            AnalyticsInterval.MONTHS -> 6
        }

        // Start from the first day of the current month for months.
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (interval == AnalyticsInterval.MONTHS) {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
        }

        for (i in 0 until numPeriods) {
            val periodCalendar = calendar.clone() as Calendar
            when (interval) {
                AnalyticsInterval.DAYS -> {
                    periodCalendar.add(Calendar.DAY_OF_YEAR, -i)
                }
                AnalyticsInterval.WEEKS -> {
                    periodCalendar.add(Calendar.WEEK_OF_YEAR, -i)
                    periodCalendar.set(Calendar.DAY_OF_WEEK, periodCalendar.firstDayOfWeek)
                }
                AnalyticsInterval.MONTHS -> {
                    periodCalendar.add(Calendar.MONTH, -i)
                    periodCalendar.set(Calendar.DAY_OF_MONTH, 1)
                }
            }

            periodCalendar.set(Calendar.HOUR_OF_DAY, 0)
            periodCalendar.set(Calendar.MINUTE, 0)
            periodCalendar.set(Calendar.SECOND, 0)
            periodCalendar.set(Calendar.MILLISECOND, 0)

            periods.add(periodCalendar.timeInMillis)
            labels.add(dateFormat.format(periodCalendar.time).lowercase(Locale.getDefault()))
        }

        // Since we collected periods from current to past, we need to reverse the lists
        periods.reverse()
        labels.reverse()

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

data class AnalyticsData(
    val totalMeditationTime: List<Float> = emptyList(),
    val totalTime: List<Float> = emptyList(),
    val sessions: List<Float> = emptyList(),
    val pauses: List<Float> = emptyList(),
    val rating: List<Float> = emptyList(),
    val score: List<Float> = emptyList(),     // ← new field
    val xAxisLabels: List<String> = emptyList()
)