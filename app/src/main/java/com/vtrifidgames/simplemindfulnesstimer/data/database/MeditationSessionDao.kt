package com.vtrifidgames.simplemindfulnesstimer.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationSessionDao {

    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSession): Long

    @Delete
    suspend fun deleteSession(session: MeditationSession)

    @Query("DELETE FROM meditation_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    // New: Update a session.
    @Update
    suspend fun updateSession(session: MeditationSession)

    // Get a session by id.
    @Query("SELECT * FROM meditation_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): MeditationSession?
}

