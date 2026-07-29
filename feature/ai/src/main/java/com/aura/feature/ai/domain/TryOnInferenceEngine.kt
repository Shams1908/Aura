package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.feature.ai.model.PoseLandmarks

data class TryOnResult(
    val outputBitmap: Bitmap,
    val fps: Float
)

interface TryOnInferenceEngine {
    /**
     * Executes real-time overlay or local 2D deformation depending on models loaded.
     */
    suspend fun processFrame(
        cameraFrame: Bitmap,
        poseLandmarks: PoseLandmarks,
        garment: ParsedGarment
    ): Result<TryOnResult>
}
