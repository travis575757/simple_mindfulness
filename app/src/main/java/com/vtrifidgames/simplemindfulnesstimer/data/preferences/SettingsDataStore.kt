package com.vtrifidgames.simplemindfulnesstimer.datastore

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance for settings.
val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val BELL_INTERVAL = longPreferencesKey("bell_interval")
    val DEFAULT_TIMER_DURATION = longPreferencesKey("default_timer_duration")
    val USE_BELL = booleanPreferencesKey("use_bell")
}

class SettingsDataStore(private val context: Context) {
    val bellIntervalFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[SettingsKeys.BELL_INTERVAL] ?: 60L
    }

    val defaultTimerDurationFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[SettingsKeys.DEFAULT_TIMER_DURATION] ?: 300L
    }

    // New flow for the "use bell" setting. Default is true.
    val useBellFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SettingsKeys.USE_BELL] ?: true
    }

    suspend fun updateBellInterval(newInterval: Long) {
        context.dataStore.edit { preferences ->
            preferences[SettingsKeys.BELL_INTERVAL] = newInterval
        }
    }

    suspend fun updateDefaultTimerDuration(newDuration: Long) {
        context.dataStore.edit { preferences ->
            preferences[SettingsKeys.DEFAULT_TIMER_DURATION] = newDuration
        }
    }

    // New: update function for "use bell"
    suspend fun updateUseBell(newUseBell: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SettingsKeys.USE_BELL] = newUseBell
        }
    }
}
