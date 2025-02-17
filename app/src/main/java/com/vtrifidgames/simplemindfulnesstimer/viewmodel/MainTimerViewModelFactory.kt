package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer
import com.vtrifidgames.simplemindfulnesstimer.utils.AlarmPlayer

class MainTimerViewModelFactory(
    private val repository: MeditationRepository,
    private val settingsDataStore: SettingsDataStore,
    private val bellPlayer: BellPlayer,
    private val alarmPlayer: AlarmPlayer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainTimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainTimerViewModel(repository, settingsDataStore, bellPlayer, alarmPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
