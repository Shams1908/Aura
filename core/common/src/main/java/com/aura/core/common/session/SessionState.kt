package com.aura.core.common.session

/**
 * UI State representation for the active Outfit Session.
 */
data class SessionState(
    val activeSession: OutfitSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Event triggers to handle Outfit Session states.
 */
sealed interface SessionEvent {
    object CreateSession : SessionEvent
    data class LoadSession(val sessionId: OutfitSessionId) : SessionEvent
    data class AttachOutfit(val outfitUri: String, val metadata: com.aura.core.common.data.OutfitModel) : SessionEvent
    data class TransitionStage(val stage: SessionLifecycleStage) : SessionEvent
    data class UpdateCameraState(val cameraState: String) : SessionEvent
    data class UpdateTrackingState(val trackingState: String) : SessionEvent
    data class UpdateAnalysisStatus(val status: String) : SessionEvent
    data class UpdateTryOnStatus(val status: String) : SessionEvent
    data class UpdateShoppingStatus(val status: String) : SessionEvent
    object CompleteSession : SessionEvent
}
