package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.database.Rating
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer
import com.vtrifidgames.simplemindfulnesstimer.utils.AlarmPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class TimerStatus {
    STOPPED,
    RUNNING,
    PAUSED,
    FINISHED
}

data class TimerUIState(
    val timerStatus: TimerStatus = TimerStatus.STOPPED,
    val totalDuration: Long,       // in seconds
    val timeLeft: Long,            // in seconds
    val intervalBell: Long,        // in seconds
    val intervalBellEnabled: Boolean = false
)

class MainTimerViewModel(
    private val repository: MeditationRepository,
    private val settingsDataStore: SettingsDataStore,
    private val bellPlayer: BellPlayer,
    private val alarmPlayer: AlarmPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TimerUIState(
            totalDuration = 300L,
            timeLeft = 300L,
            intervalBell = 60L,
            timerStatus = TimerStatus.STOPPED,
            intervalBellEnabled = false
        )
    )
    val uiState = _uiState.asStateFlow()

    // New: Track number of pauses.
    private var pauseCount: Int = 0

    init {
        viewModelScope.launch {
            settingsDataStore.defaultTimerDurationFlow.collect { newDuration ->
                if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
                    _uiState.value = _uiState.value.copy(
                        totalDuration = newDuration,
                        timeLeft = newDuration
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.bellIntervalFlow.collect { newBell ->
                if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
                    _uiState.value = _uiState.value.copy(
                        intervalBell = newBell
                    )
                }
            }
        }
    }

    private val _navigateToSummary = MutableSharedFlow<Long>(replay = 0)
    val navigateToSummary = _navigateToSummary.asSharedFlow()

    private var sessionStartTime: Long = 0L
    private var timerJob: Job? = null

    fun startTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) return

        sessionStartTime = System.currentTimeMillis()
        pauseCount = 0  // Reset pause count when starting.

        if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.totalDuration)
        }

        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)

        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                delay(1000)
                val currentTimeLeft = _uiState.value.timeLeft - 1
                _uiState.value = _uiState.value.copy(timeLeft = currentTimeLeft)

                if (_uiState.value.intervalBellEnabled &&
                    currentTimeLeft > 0 &&
                    (currentTimeLeft % _uiState.value.intervalBell == 0L)
                ) {
                    playIntervalBell()
                }
            }
            if (_uiState.value.timeLeft <= 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.FINISHED)
                alarmPlayer.playAlarm()
            }
        }
    }

    fun pauseTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
            _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.PAUSED)
            pauseCount++ // Increment pause count on pause.
        }
    }

    fun resumeTimer() {
        if (_uiState.value.timerStatus == TimerStatus.PAUSED) {
            _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)
            timerJob = viewModelScope.launch {
                while (_uiState.value.timeLeft > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                    delay(1000)
                    val currentTimeLeft = _uiState.value.timeLeft - 1
                    _uiState.value = _uiState.value.copy(timeLeft = currentTimeLeft)

                    if (_uiState.value.intervalBellEnabled &&
                        currentTimeLeft > 0 &&
                        (currentTimeLeft % _uiState.value.intervalBell == 0L)
                    ) {
                        playIntervalBell()
                    }
                }
                if (_uiState.value.timeLeft <= 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                    _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.FINISHED)
                    alarmPlayer.playAlarm()
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.STOPPED,
            timeLeft = _uiState.value.totalDuration
        )
    }

    /**
     * Called when the user presses the Finish button.
     * This stops the alarm and emits a navigation event.
     */
    fun finishSession() {
        alarmPlayer.stopAlarm()
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.STOPPED)
        val sessionEndTime = System.currentTimeMillis()
        val durationTotal = TimeUnit.MILLISECONDS.toSeconds(sessionEndTime - sessionStartTime)
        // Calculate meditated time as the intended duration minus the time left.
        val durationMeditated = _uiState.value.totalDuration - _uiState.value.timeLeft
        viewModelScope.launch {
            val insertedId = repository.insert(
                MeditationSession(
                    date = sessionEndTime,
                    time = sessionStartTime,
                    durationTotal = durationTotal,
                    durationMeditated = durationMeditated,
                    pauses = pauseCount,
                    rating = Rating.AVERAGE, // Default rating
                    notes = null
                )
            )
            _navigateToSummary.emit(insertedId)
        }
    }

    private fun playIntervalBell() {
        bellPlayer.playBell()
    }

    fun setIntervalBell(enabled: Boolean, intervalSeconds: Long) {
        _uiState.value = _uiState.value.copy(
            intervalBellEnabled = enabled,
            intervalBell = intervalSeconds
        )
    }
}
