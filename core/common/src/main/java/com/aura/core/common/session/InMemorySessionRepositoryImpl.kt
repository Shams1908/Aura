package com.aura.core.common.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe, in-memory implementation of [SessionRepository].
 */
@Singleton
class InMemorySessionRepositoryImpl @Inject constructor() : SessionRepository {

    private val sessions = ConcurrentHashMap<String, OutfitSession>()
    private val sessionsFlow = MutableStateFlow<Map<String, OutfitSession>>(emptyMap())
    private val activeSessionIdFlow = MutableStateFlow<OutfitSessionId?>(null)

    override fun getSession(sessionId: OutfitSessionId): Flow<OutfitSession?> {
        return sessionsFlow.map { it[sessionId.value] }
    }

    override suspend fun saveSession(session: OutfitSession) {
        sessions[session.sessionId.value] = session
        sessionsFlow.value = sessions.toMap()
    }

    override suspend fun deleteSession(sessionId: OutfitSessionId) {
        sessions.remove(sessionId.value)
        sessionsFlow.value = sessions.toMap()
        if (activeSessionIdFlow.value == sessionId) {
            activeSessionIdFlow.value = null
        }
    }

    override fun getActiveSessionId(): Flow<OutfitSessionId?> {
        return activeSessionIdFlow
    }

    override suspend fun setActiveSessionId(sessionId: OutfitSessionId?) {
        activeSessionIdFlow.value = sessionId
    }
}
