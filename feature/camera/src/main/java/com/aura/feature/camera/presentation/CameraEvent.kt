package com.aura.feature.camera.presentation

import android.net.Uri

/**
 * Event actions dispatched from the camera viewport.
 */
sealed interface CameraEvent {
    data class PermissionResult(val isGranted: Boolean) : CameraEvent
    object SwitchCamera : CameraEvent
    data class ToggleFlash(val isEnabled: Boolean) : CameraEvent
    data class SetZoom(val ratio: Float) : CameraEvent
    object CapturePhoto : CameraEvent
    data class PhotoCaptured(val uri: Uri) : CameraEvent
    data class CaptureError(val errorMsg: String) : CameraEvent
    object ResetPreview : CameraEvent
}
