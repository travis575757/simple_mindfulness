package com.vtrifidgames.simplemindfulnesstimer.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,       // Unix timestamp in milliseconds
    val duration: Long,   // Duration in seconds
    val notes: String? = null
)

