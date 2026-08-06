package com.aura.core.common.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Common Viewmodel facilitating reactive state observation of the active Outfit Session.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: StateFlow<SessionState> = sessionManager.activeSession
        .map { session ->
            SessionState(activeSession = session)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionState()
        )

    /**
     * Dispatch session events to update current active workspace status.
     */
    fun onEvent(event: SessionEvent) {
        viewModelScope.launch {
            when (event) {
                is SessionEvent.CreateSession -> {
                    sessionManager.createNewSession()
                }
                is SessionEvent.LoadSession -> {
                    sessionManager.loadSession(event.sessionId)
                }
                is SessionEvent.AttachOutfit -> {
                    sessionManager.attachOutfit(event.outfitUri, event.metadata)
                }
                is SessionEvent.TransitionStage -> {
                    sessionManager.transitionStage(event.stage)
                }
                is SessionEvent.UpdateCameraState -> {
                    sessionManager.updateCameraState(event.cameraState)
                }
                is SessionEvent.UpdateTrackingState -> {
                    sessionManager.updateTrackingState(event.trackingState)
                }
                is SessionEvent.UpdateAnalysisStatus -> {
                    sessionManager.updateAnalysisStatus(event.status)
                }
                is SessionEvent.UpdateTryOnStatus -> {
                    sessionManager.updateTryOnStatus(event.status)
                }
                is SessionEvent.UpdateShoppingStatus -> {
                    sessionManager.updateShoppingStatus(event.status)
                }
                is SessionEvent.CompleteSession -> {
                    sessionManager.completeSession()
                }
            }
        }
    }
}
