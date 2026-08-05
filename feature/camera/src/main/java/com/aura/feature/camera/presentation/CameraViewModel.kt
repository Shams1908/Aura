package com.aura.feature.camera.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel orchestrating camera UI status events and layout selections.
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /**
     * Entry point to handle screen actions dispatched from Compose elements.
     */
    fun onEvent(event: CameraEvent) {
        when (event) {
            is CameraEvent.PermissionResult -> {
                _uiState.update {
                    it.copy(
                        hasPermission = event.isGranted,
                        status = if (event.isGranted) CameraStatus.READY else CameraStatus.PERMISSION_REQUIRED
                    )
                }
            }
            is CameraEvent.SwitchCamera -> {
                _uiState.update {
                    it.copy(isFrontCamera = !it.isFrontCamera)
                }
            }
            is CameraEvent.ToggleFlash -> {
                _uiState.update {
                    it.copy(isFlashEnabled = event.isEnabled)
                }
            }
            is CameraEvent.SetZoom -> {
                _uiState.update {
                    it.copy(zoomRatio = event.ratio)
                }
            }
            is CameraEvent.CapturePhoto -> {
                _uiState.update {
                    it.copy(status = CameraStatus.CAPTURING)
                }
            }
            is CameraEvent.PhotoCaptured -> {
                _uiState.update {
                    it.copy(
                        status = CameraStatus.PREVIEW_MODE,
                        capturedImageUri = event.uri
                    )
                }
            }
            is CameraEvent.CaptureError -> {
                _uiState.update {
                    it.copy(
                        status = CameraStatus.READY,
                        errorMessage = event.errorMsg
                    )
                }
            }
            is CameraEvent.ResetPreview -> {
                _uiState.update {
                    it.copy(
                        status = CameraStatus.READY,
                        capturedImageUri = null
                    )
                }
            }
        }
    }
}
