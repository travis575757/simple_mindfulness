package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer
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
    PAUSED
}

// TimerUIState now takes its default values as parameters.
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
    private val bellPlayer: BellPlayer
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

    // SharedFlow for one-time navigation events.
    private val _navigateToSummary = MutableSharedFlow<Long>(replay = 0)
    val navigateToSummary = _navigateToSummary.asSharedFlow()

    private var sessionStartTime: Long = 0L
    private var timerJob: Job? = null

    fun startTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) return

        sessionStartTime = System.currentTimeMillis()

        // Reset timeLeft if starting from STOPPED.
        if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.totalDuration)
        }

        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)

        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                delay(1000)
                val currentTimeLeft = _uiState.value.timeLeft - 1
                _uiState.value = _uiState.value.copy(timeLeft = currentTimeLeft)

                // Play an interval bell if enabled.
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

    fun pauseTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
            _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.PAUSED)
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
                    completeSession()
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
     * Completes the session by saving it and emitting a navigation event.
     */
    private fun completeSession() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.STOPPED)
        val sessionEndTime = System.currentTimeMillis()
        val totalDurationMillis = sessionEndTime - sessionStartTime
        val durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalDurationMillis)

        viewModelScope.launch {
            val insertedId = repository.insert(
                MeditationSession(
                    date = sessionEndTime,
                    duration = durationInSeconds,
                    notes = null
                )
            )
            _navigateToSummary.emit(insertedId)
        }
    }

    /**
     * Play the interval bell sound using BellPlayer.
     */
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
