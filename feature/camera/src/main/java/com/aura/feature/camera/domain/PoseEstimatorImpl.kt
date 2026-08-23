package com.aura.feature.camera.domain

import androidx.camera.core.CameraSelector
import com.aura.core.vision.model.PoseResult
import com.aura.core.vision.model.VisionFrame
import com.aura.core.vision.service.PoseEstimator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseEstimatorImpl @Inject constructor(
    private val poseDetectorEngine: PoseDetectorEngine
) : PoseEstimator {

    override suspend fun estimatePose(frame: VisionFrame): PoseResult {
        val result = poseDetectorEngine.detectPose(
            bitmap = frame.bitmap,
            rotationDegrees = frame.metadata.rotationDegrees,
            timestampMs = frame.metadata.timestampMs
        )
        
        val bodyPose = result.getOrNull()
        val landmarks = bodyPose?.landmarks?.map { landmark ->
            val id = when (landmark.name) {
                "left_shoulder" -> 11
                "right_shoulder" -> 12
                "left_elbow" -> 13
                "right_elbow" -> 14
                "left_wrist" -> 15
                "right_wrist" -> 16
                "left_hip" -> 23
                "right_hip" -> 24
                else -> -1
            }
            PoseResult.PoseLandmark(
                id = id,
                x = landmark.x,
                y = landmark.y,
                z = landmark.z ?: 0.0f,
                likelihood = landmark.likelihood
            )
        } ?: emptyList()

        return PoseResult(
            landmarks = landmarks,
            imageWidth = frame.metadata.width,
            imageHeight = frame.metadata.height,
            rotationDegrees = frame.metadata.rotationDegrees,
            isFrontCamera = frame.metadata.lensFacing == CameraSelector.LENS_FACING_FRONT
        )
    }
}
