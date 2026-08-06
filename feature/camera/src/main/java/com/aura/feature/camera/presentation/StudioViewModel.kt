package com.aura.feature.camera.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

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
class StudioViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    fun onEvent(event: StudioEvent) {
        when (event) {
            is StudioEvent.PermissionResult -> {
                _uiState.update {
                    it.copy(
                        hasPermission = event.isGranted,
                        status = if (event.isGranted) StudioStatus.CAMERA_READY else StudioStatus.TRACKING_WAITING
                    )
                }
            }
            is StudioEvent.SwitchCamera -> {
                _uiState.update {
                    it.copy(isFrontCamera = !it.isFrontCamera)
                }
            }
            is StudioEvent.ToggleFlash -> {
                _uiState.update {
                    it.copy(isFlashEnabled = event.isEnabled)
                }
            }
            is StudioEvent.SetZoom -> {
                _uiState.update {
                    it.copy(zoomRatio = event.ratio)
                }
            }
            is StudioEvent.CapturePhoto -> {
                // Capturing state handled internally by camera preview capture action
            }
            is StudioEvent.PhotoCaptured -> {
                _uiState.update {
                    it.copy(
                        capturedImageUri = event.uri
                    )
                }
            }
            is StudioEvent.CaptureError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = event.errorMsg
                    )
                }
            }
            is StudioEvent.ResetPreview -> {
                _uiState.update {
                    it.copy(
                        capturedImageUri = null
                    )
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
            }
        }
    }
}
