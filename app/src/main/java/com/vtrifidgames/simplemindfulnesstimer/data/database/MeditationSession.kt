package com.vtrifidgames.simplemindfulnesstimer.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The new MeditationSession entity now stores:
 * - id: The primary key.
 * - date: The finishing timestamp (Unix milliseconds).
 * - time: The starting timestamp of the session.
 * - durationTotal: The total elapsed time (in seconds) from start to finish (including pauses).
 * - durationMeditated: The actual meditated time (in seconds), i.e. the time during which the timer was running.
 * - pauses: The number of times the user paused/resumed.
 * - rating: A user rating (an enum with five possible values).
 * - notes: Optional notes.
 */
@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,            // Finishing timestamp
    val time: Long,            // Starting timestamp
    val durationTotal: Long,   // Total elapsed time (seconds)
    val durationMeditated: Long,  // Actual meditated time (seconds)
    val pauses: Int,           // Number of pauses
    val rating: Rating,        // User rating
    val notes: String? = null
)
