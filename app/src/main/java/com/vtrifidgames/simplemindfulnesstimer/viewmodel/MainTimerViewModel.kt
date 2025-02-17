package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Represents the status of the timer.
 */
enum class TimerStatus {
    STOPPED,
    RUNNING,
    PAUSED
}

/**
 * Holds UI-related data for the timer screen.
 */
data class TimerUIState(
    val timerStatus: TimerStatus = TimerStatus.STOPPED,
    val totalDuration: Long = 300L, // total session time in seconds (example: 5 minutes)
    val timeLeft: Long = 300L,     // how many seconds remain
    val intervalBell: Long = 60L,  // play a bell every 60 seconds (placeholder)
    val intervalBellEnabled: Boolean = false
)

/**
 * MainTimerViewModel manages the timer logic and saves completed sessions to the database.
 */
class MainTimerViewModel(private val repository: MeditationRepository) : ViewModel() {

    // Expose UI state as a StateFlow for Composables to observe.
    private val _uiState = MutableStateFlow(TimerUIState())
    val uiState = _uiState.asStateFlow()

    // Track the start time for calculating actual duration.
    private var sessionStartTime: Long = 0L

    // Coroutine job for the timer.
    private var timerJob: Job? = null

    /**
     * Starts or restarts a timer with the current [TimerUIState.totalDuration].
     */
    fun startTimer() {
        // If the timer is already running, do nothing.
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) return

        // Initialize or re-initialize session data
        sessionStartTime = System.currentTimeMillis()

        // Reset timeLeft to totalDuration if we were STOPPED
        if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.totalDuration)
        }

        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)

        // Launch a coroutine to count down every second.
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                delay(1000)
                val currentTimeLeft = _uiState.value.timeLeft - 1
                _uiState.value = _uiState.value.copy(timeLeft = currentTimeLeft)

                // Check if we need to play an interval bell.
                if (_uiState.value.intervalBellEnabled &&
                    currentTimeLeft > 0 &&
                    (currentTimeLeft % _uiState.value.intervalBell == 0L)
                ) {
                    playIntervalBell()
                }
            }
            // If timeLeft reaches 0 while RUNNING, session is complete.
            if (_uiState.value.timeLeft <= 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                completeSession()
            }
        }
    }

    /**
     * Pauses the current timer if it's running.
     */
    fun pauseTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
            _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.PAUSED)
        }
    }

    /**
     * Resumes the timer if it's paused.
     */
    fun resumeTimer() {
        if (_uiState.value.timerStatus == TimerStatus.PAUSED) {
            _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)
            // Restart the coroutine loop
            timerJob = viewModelScope.launch {
                while (_uiState.value.timeLeft > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                    delay(1000)
                    val currentTimeLeft = _uiState.value.timeLeft - 1
                    _uiState.value = _uiState.value.copy(timeLeft = currentTimeLeft)

                    // Interval bell logic
                    if (_uiState.value.intervalBellEnabled &&
                        currentTimeLeft > 0 &&
                        (currentTimeLeft % _uiState.value.intervalBell == 0L)
                    ) {
                        playIntervalBell()
                    }
                }
                if (_uiState.value.timeLeft <= 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                    completeSession()
                }
            }
        }
    }

    /**
     * Resets the timer back to a STOPPED state.
     */
    fun resetTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.STOPPED,
            timeLeft = _uiState.value.totalDuration
        )
    }

    /**
     * Called when the timer completes or manually ended.
     * Saves the session to the DB.
     */
    private fun completeSession() {
        // Stop the timer job.
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.STOPPED)

        val sessionEndTime = System.currentTimeMillis()
        val totalDuration = sessionEndTime - sessionStartTime
        val durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration)

        // Insert a new MeditationSession into the DB
        viewModelScope.launch {
            repository.insert(
                MeditationSession(
                    date = sessionEndTime,
                    duration = durationInSeconds,
                    notes = null // or prompt the user for notes
                )
            )
        }
    }

    /**
     * Placeholder function for interval bell logic.
     * In a real app, you'd trigger a sound effect here.
     */
    private fun playIntervalBell() {
        // E.g., play a chime sound using MediaPlayer or SoundPool
        // For now, this is just a placeholder.
    }

    /**
     * Update whether interval bells are enabled and/or the interval length.
     */
    fun setIntervalBell(enabled: Boolean, intervalSeconds: Long) {
        _uiState.value = _uiState.value.copy(
            intervalBellEnabled = enabled,
            intervalBell = intervalSeconds
        )
    }
}

