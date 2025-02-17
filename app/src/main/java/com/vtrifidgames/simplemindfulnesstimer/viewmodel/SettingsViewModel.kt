package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val dataStore: SettingsDataStore) : ViewModel() {

    // Expose the settings as StateFlows.
    val bellInterval: StateFlow<Long> = dataStore.bellIntervalFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 60L)

    val defaultTimerDuration: StateFlow<Long> = dataStore.defaultTimerDurationFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 300L)

    fun updateBellInterval(newInterval: Long) {
        viewModelScope.launch {
            dataStore.updateBellInterval(newInterval)
        }
    }

    fun updateDefaultTimerDuration(newDuration: Long) {
        viewModelScope.launch {
            dataStore.updateDefaultTimerDuration(newDuration)
        }
    }
}
