package com.vtrifidgames.simplemindfulnesstimer.data.repository

import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSession
import com.vtrifidgames.simplemindfulnesstimer.data.database.MeditationSessionDao
import kotlinx.coroutines.flow.Flow

class MeditationRepository(private val dao: MeditationSessionDao) {

    val allSessions: Flow<List<MeditationSession>> = dao.getAllSessions()

    suspend fun insert(session: MeditationSession): Long {
        return dao.insertSession(session)
    }

    suspend fun delete(session: MeditationSession) {
        dao.deleteSession(session)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteSessionById(id)
    }

    suspend fun update(session: MeditationSession) {
        dao.updateSession(session)
    }

    suspend fun getSessionById(id: Long): MeditationSession? {
        return dao.getSessionById(id)
    }
}
