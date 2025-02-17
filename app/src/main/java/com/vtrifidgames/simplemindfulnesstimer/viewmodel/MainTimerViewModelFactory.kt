package com.vtrifidgames.simplemindfulnesstimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtrifidgames.simplemindfulnesstimer.data.repository.MeditationRepository
import com.vtrifidgames.simplemindfulnesstimer.datastore.SettingsDataStore
import com.vtrifidgames.simplemindfulnesstimer.utils.BellPlayer

class MainTimerViewModelFactory(
    private val repository: MeditationRepository,
    private val settingsDataStore: SettingsDataStore,
    private val bellPlayer: BellPlayer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainTimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainTimerViewModel(repository, settingsDataStore, bellPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
