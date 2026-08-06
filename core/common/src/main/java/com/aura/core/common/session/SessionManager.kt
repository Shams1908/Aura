package com.aura.core.common.session

import com.aura.core.common.data.OutfitModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager orchestrating the active try-on session lifecycle and coordinating state updates.
 */
interface SessionManager {
    /**
     * Exposes the active outfit session as a read-only StateFlow.
     */
    val activeSession: StateFlow<OutfitSession?>

    /**
     * Initializes a new unique try-on Outfit Session.
     */
    suspend fun createNewSession(): OutfitSession

    /**
     * Attaches outfit reference details and metadata to the active session.
     */
    suspend fun attachOutfit(outfitUri: String, metadata: OutfitModel)

    /**
     * Transitions the session to a different lifecycle stage.
     */
    suspend fun transitionStage(newStage: SessionLifecycleStage)

    /**
     * Updates the current camera operational state.
     */
    suspend fun updateCameraState(state: String)

    /**
     * Updates the posture tracking indicator state.
     */
    suspend fun updateTrackingState(state: String)

    /**
     * Updates the digital fit analysis status.
     */
    suspend fun updateAnalysisStatus(status: String)

    /**
     * Updates the simulated AI try-on overlay status.
     */
    suspend fun updateTryOnStatus(status: String)

    /**
     * Updates the checkout/shopping catalog readiness.
     */
    suspend fun updateShoppingStatus(status: String)

    /**
     * Finishes and clears the active session workspace.
     */
    suspend fun completeSession()

    /**
     * Reloads and binds to an existing session workspace.
     */
    suspend fun loadSession(sessionId: OutfitSessionId)
}
