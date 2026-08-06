package com.aura.feature.camera.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aura.core.common.session.SessionManager
import com.aura.core.common.session.OutfitSessionId
import com.aura.core.common.session.SessionLifecycleStage

/**
 * Event actions dispatched from the Aura Studio screen.
 */
sealed interface StudioEvent {
    data class PermissionResult(val isGranted: Boolean) : StudioEvent
    object SwitchCamera : StudioEvent
    data class ToggleFlash(val isEnabled: Boolean) : StudioEvent
    data class SetZoom(val ratio: Float) : StudioEvent
    object CapturePhoto : StudioEvent
    data class PhotoCaptured(val uri: Uri) : StudioEvent
    data class CaptureError(val errorMsg: String) : StudioEvent
    object ResetPreview : StudioEvent
    data class SetBottomSheetExpanded(val expanded: Boolean) : StudioEvent
    data class SetStatus(val status: StudioStatus) : StudioEvent
}

/**
 * ViewModel orchestrating the camera studio UI state and events.
 */
@HiltViewModel
class StudioViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        // Collect active session from SessionManager and sync relevant UI properties
        viewModelScope.launch {
            sessionManager.activeSession.collect { session ->
                if (session != null) {
                    _uiState.update { state ->
                        state.copy(
                            zoomRatio = session.cameraState.toFloatOrNull() ?: state.zoomRatio,
                            capturedImageUri = session.referenceOutfitUri?.let { Uri.parse(it) } ?: state.capturedImageUri,
                            status = when (session.stage) {
                                SessionLifecycleStage.CAMERA_READY -> StudioStatus.CAMERA_READY
                                SessionLifecycleStage.TRACKING_READY -> StudioStatus.TRACKING_WAITING
                                SessionLifecycleStage.OUTFIT_ATTACHED -> StudioStatus.OUTFIT_LOADED
                                else -> state.status
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Bind camera workspace to active session workspace ID.
     */
    fun loadSession(sessionId: OutfitSessionId) {
        viewModelScope.launch {
            sessionManager.loadSession(sessionId)
            sessionManager.transitionStage(SessionLifecycleStage.STUDIO_OPENED)
        }
    }

    fun onEvent(event: StudioEvent) {
        when (event) {
            is StudioEvent.PermissionResult -> {
                _uiState.update {
                    it.copy(
                        hasPermission = event.isGranted,
                        status = if (event.isGranted) StudioStatus.CAMERA_READY else StudioStatus.TRACKING_WAITING
                    )
                }
                viewModelScope.launch {
                    sessionManager.updateCameraState(if (event.isGranted) "READY" else "PERMISSION_REQUIRED")
                    sessionManager.transitionStage(
                        if (event.isGranted) SessionLifecycleStage.CAMERA_READY else SessionLifecycleStage.TRACKING_READY
                    )
                }
            }
            is StudioEvent.SwitchCamera -> {
                val nextFront = !_uiState.value.isFrontCamera
                _uiState.update {
                    it.copy(isFrontCamera = nextFront)
                }
                viewModelScope.launch {
                    sessionManager.updateCameraState("LENS:${if (nextFront) "FRONT" else "BACK"}")
                }
            }
            is StudioEvent.ToggleFlash -> {
                _uiState.update {
                    it.copy(isFlashEnabled = event.isEnabled)
                }
                viewModelScope.launch {
                    sessionManager.updateCameraState("FLASH:${if (event.isEnabled) "ON" else "OFF"}")
                }
            }
            is StudioEvent.SetZoom -> {
                _uiState.update {
                    it.copy(zoomRatio = event.ratio)
                }
                viewModelScope.launch {
                    sessionManager.updateCameraState(event.ratio.toString())
                }
            }
            is StudioEvent.CapturePhoto -> {
                viewModelScope.launch {
                    sessionManager.updateAnalysisStatus("CAPTURING")
                }
            }
            is StudioEvent.PhotoCaptured -> {
                _uiState.update {
                    it.copy(
                        capturedImageUri = event.uri
                    )
                }
                viewModelScope.launch {
                    sessionManager.attachOutfit(
                        outfitUri = event.uri.toString(),
                        metadata = com.aura.core.common.data.OutfitModel(
                            id = System.currentTimeMillis().toString(),
                            title = "Captured Fit",
                            brand = "Aura Studio",
                            description = "Visual fit analysis item",
                            imageUrl = event.uri.toString(),
                            category = "Capture",
                            color = "Multi",
                            tags = listOf("Studio"),
                            price = 0.0
                        )
                    )
                    sessionManager.updateAnalysisStatus("READY")
                    sessionManager.transitionStage(SessionLifecycleStage.ANALYSIS_READY)
                }
            }
            is StudioEvent.CaptureError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = event.errorMsg
                    )
                }
                viewModelScope.launch {
                    sessionManager.updateAnalysisStatus("ERROR: ${event.errorMsg}")
                }
            }
            is StudioEvent.ResetPreview -> {
                _uiState.update {
                    it.copy(
                        capturedImageUri = null
                    )
                }
                viewModelScope.launch {
                    sessionManager.updateAnalysisStatus("RESET")
                    sessionManager.transitionStage(SessionLifecycleStage.CAMERA_READY)
                }
            }
            is StudioEvent.SetBottomSheetExpanded -> {
                _uiState.update {
                    it.copy(isBottomSheetExpanded = event.expanded)
                }
            }
            is StudioEvent.SetStatus -> {
                _uiState.update {
                    it.copy(status = event.status)
                }
                viewModelScope.launch {
                    val nextStage = when (event.status) {
                        StudioStatus.CAMERA_READY -> SessionLifecycleStage.CAMERA_READY
                        StudioStatus.TRACKING_WAITING -> SessionLifecycleStage.TRACKING_READY
                        StudioStatus.OUTFIT_LOADED -> SessionLifecycleStage.OUTFIT_ATTACHED
                    }
                    sessionManager.transitionStage(nextStage)
                }
            }
        }
    }
}
