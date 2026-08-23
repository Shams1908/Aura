package com.aura.feature.camera.domain

import android.graphics.Bitmap
import kotlinx.coroutines.flow.StateFlow

interface PoseDetectorEngine {
    val poseResult: StateFlow<BodyPoseResult?>
    suspend fun detectPose(bitmap: Bitmap, rotationDegrees: Int, timestampMs: Long): Result<BodyPoseResult>
    fun close()
}
