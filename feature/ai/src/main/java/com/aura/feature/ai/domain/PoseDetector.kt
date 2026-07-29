package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.feature.ai.model.PoseLandmarks
import kotlinx.coroutines.flow.Flow

interface PoseDetector {
    suspend fun detectPose(bitmap: Bitmap): Result<PoseLandmarks>
    fun getLivePoseFlow(): Flow<PoseLandmarks>
    fun close()
}
