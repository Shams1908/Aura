package com.aura.core.common.session

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing persistency or in-memory caching of Outfit Sessions.
 */
interface SessionRepository {
    /**
     * Retrieves an [OutfitSession] by its [OutfitSessionId] as a cold stream.
     */
    fun getSession(sessionId: OutfitSessionId): Flow<OutfitSession?>

    /**
     * Saves or updates an [OutfitSession].
     */
    suspend fun saveSession(session: OutfitSession)

    /**
     * Removes an [OutfitSession] from storage.
     */
    suspend fun deleteSession(sessionId: OutfitSessionId)

    /**
     * Observes the active [OutfitSessionId] if any session is in progress.
     */
    fun getActiveSessionId(): Flow<OutfitSessionId?>

    /**
     * Configures the current active [OutfitSessionId].
     */
    suspend fun setActiveSessionId(sessionId: OutfitSessionId?)
}
