package com.vtrifidgames.simplemindfulnesstimer.data.repository

import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSessionDao
import kotlinx.coroutines.flow.Flow

class MeditationRepository(private val dao: MeditationSessionDao) {

    // Observe all sessions as a Flow
    val allSessions: Flow<List<MeditationSession>> = dao.getAllSessions()

    // Insert a new session
    suspend fun insert(session: MeditationSession) {
        dao.insertSession(session)
    }

    // Delete a session by providing the full object
    suspend fun delete(session: MeditationSession) {
        dao.deleteSession(session)
    }

    // Delete a session by its id (alternative)
    suspend fun deleteById(id: Int) {
        dao.deleteSessionById(id)
    }
}

