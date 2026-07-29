package com.aura.feature.camera.domain

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.SharedFlow

sealed interface CameraState {
    object Idle : CameraState
    object Initializing : CameraState
    object Active : CameraState
    data class Error(val exception: Throwable) : CameraState
}

interface CameraPipelineManager {
    val cameraState: SharedFlow<CameraState>

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: androidx.camera.core.Preview.SurfaceProvider,
        imageAnalyzer: ImageAnalysis.Analyzer
    )

    fun unbindCamera()
    
    fun toggleFlash(enabled: Boolean)
}
