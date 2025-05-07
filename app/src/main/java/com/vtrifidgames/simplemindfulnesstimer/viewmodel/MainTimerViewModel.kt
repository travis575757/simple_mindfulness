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
    private var targetTime: Long = 0L

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
        viewModelScope.launch {
            settingsDataStore.useBellFlow.collect { useBell ->
                _uiState.value = _uiState.value.copy(
                    intervalBellEnabled = useBell
                )
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
        pauseCount = 0

        // Reset timeLeft if we're coming from a stopped state
        if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.totalDuration)
        }

        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)

        // Initialize the mutable targetTime
        targetTime = System.currentTimeMillis() + _uiState.value.totalDuration * 1000L

        timerJob = viewModelScope.launch {
            while (_uiState.value.timerStatus == TimerStatus.RUNNING) {
                val remainingSeconds = ((targetTime - System.currentTimeMillis()) / 1000)
                    .coerceAtLeast(0L)
                _uiState.value = _uiState.value.copy(timeLeft = remainingSeconds)

                // Play interval bell if enabled and on the exact second
                if (_uiState.value.intervalBellEnabled &&
                    remainingSeconds > 0 &&
                    remainingSeconds % _uiState.value.intervalBell == 0L
                ) {
                    playIntervalBell()
                }

                if (remainingSeconds <= 0) break
                delay(200L)  // smoother UI updates
            }

            if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
                _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.FINISHED)
                alarmPlayer.playAlarm()
            }
        }
    }

    fun addMinute() {
        targetTime += 60_000L
        val newLeft = ((targetTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0L)
        _uiState.value = _uiState.value.copy(timeLeft = newLeft, totalDuration = _uiState.value.totalDuration + 60)
    }

    fun pauseTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
            // Cancel the timer job immediately so no extra delay occurs.
            timerJob?.cancel()
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

    fun setTotalDuration(newDuration: Long) {
        if (_uiState.value.timerStatus == TimerStatus.STOPPED) {
            _uiState.value = _uiState.value.copy(
                totalDuration = newDuration,
                timeLeft = newDuration
            )
        }
    }

    /**
     * Called when the user presses the Finish button.
     * This stops the alarm and emits a navigation event.
     */
    fun finishSession() {
        alarmPlayer.stopAlarm()
        timerJob?.cancel()
        val sessionEndTime = System.currentTimeMillis()
        val durationTotal = TimeUnit.MILLISECONDS.toSeconds(sessionEndTime - sessionStartTime)
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
