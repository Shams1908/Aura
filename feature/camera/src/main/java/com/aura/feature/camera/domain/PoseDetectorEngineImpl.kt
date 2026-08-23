package com.aura.feature.camera.domain

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.google.mlkit.vision.pose.PoseLandmark as MlKitPoseLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class PoseDetectorEngineImpl @Inject constructor() : PoseDetectorEngine {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private var detector: com.google.mlkit.vision.pose.PoseDetector? = null
    private val poseSmoother = PoseSmoother(alpha = 0.35f)

    private fun getDetector(): com.google.mlkit.vision.pose.PoseDetector {
        return detector ?: synchronized(this) {
            detector ?: PoseDetection.getClient(options).also { detector = it }
        }
    }

    private val _poseResult = MutableStateFlow<BodyPoseResult?>(null)
    override val poseResult: StateFlow<BodyPoseResult?> = _poseResult.asStateFlow()

    override suspend fun detectPose(
        bitmap: Bitmap,
        rotationDegrees: Int,
        timestampMs: Long
    ): Result<BodyPoseResult> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
            val currentDetector = getDetector()
            val mlKitPose = suspendCancellableCoroutine { continuation ->
                currentDetector.process(inputImage)
                    .addOnSuccessListener { pose ->
                        if (continuation.isActive) {
                            continuation.resume(pose)
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
            }

            val landmarks = mutableListOf<BodyLandmark>()
            val landmarkTypes = listOf(
                MlKitPoseLandmark.LEFT_SHOULDER to "left_shoulder",
                MlKitPoseLandmark.RIGHT_SHOULDER to "right_shoulder",
                MlKitPoseLandmark.LEFT_ELBOW to "left_elbow",
                MlKitPoseLandmark.RIGHT_ELBOW to "right_elbow",
                MlKitPoseLandmark.LEFT_WRIST to "left_wrist",
                MlKitPoseLandmark.RIGHT_WRIST to "right_wrist",
                MlKitPoseLandmark.LEFT_HIP to "left_hip",
                MlKitPoseLandmark.RIGHT_HIP to "right_hip"
            )

            for ((type, name) in landmarkTypes) {
                val lm = mlKitPose.getPoseLandmark(type)
                if (lm != null) {
                    val smoothedOffset = poseSmoother.smooth(type, lm.position.x, lm.position.y)
                    landmarks.add(
                        BodyLandmark(
                            name = name,
                            x = smoothedOffset.x,
                            y = smoothedOffset.y,
                            z = lm.position3D.z,
                            likelihood = lm.inFrameLikelihood
                        )
                    )
                }
            }

            val bodyPoseResult = BodyPoseResult(
                landmarks = landmarks,
                timestampMs = timestampMs
            )
            _poseResult.value = bodyPoseResult
            Result.success(bodyPoseResult)
        } catch (e: Exception) {
            Log.e("PoseDetectorEngine", "Error detecting pose", e)
            Result.failure(e)
        }
    }

    override fun close() {
        synchronized(this) {
            detector?.close()
            detector = null
            poseSmoother.reset()
        }
    }
}
