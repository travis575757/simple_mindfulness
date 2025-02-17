package com.vtrifidgames.simplemindfulnesstimer.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationSessionDao {

    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSession)

    // Delete a session by providing the entire object.
    @Delete
    suspend fun deleteSession(session: MeditationSession)

    // Alternatively, delete a session by its id.
    @Query("DELETE FROM meditation_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)
}

