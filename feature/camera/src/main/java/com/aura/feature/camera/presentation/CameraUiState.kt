package com.aura.feature.camera.presentation

import android.net.Uri

/**
 * Modes describing current camera operational state.
 */
enum class CameraStatus {
    INITIALIZING,
    READY,
    PERMISSION_REQUIRED,
    CAPTURING,
    PREVIEW_MODE
}

/**
 * Screen state model for the Camera studio.
 */
data class CameraUiState(
    val status: CameraStatus = CameraStatus.INITIALIZING,
    val isFrontCamera: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val capturedImageUri: Uri? = null,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null
)
