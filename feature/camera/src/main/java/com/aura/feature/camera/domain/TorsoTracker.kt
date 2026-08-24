package com.aura.feature.camera.domain

import androidx.compose.ui.geometry.Offset
import com.aura.core.vision.model.PoseResult
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TorsoBounds(
    val leftShoulder: Offset,
    val rightShoulder: Offset,
    val leftHip: Offset,
    val rightHip: Offset,
    val center: Offset,
    val shoulderWidth: Float,
    val torsoWidth: Float,
    val torsoHeight: Float,
    val rotation: Float, // roll in degrees
    val confidence: Float
)

data class GarmentAnchorPoints(
    val bounds: TorsoBounds,
    val isTrackingActive: Boolean,
    val trackingLostTimeMs: Long
)

class TorsoTracker(
    private val confidenceThreshold: Float = 0.5f,
    private val smoothingFactor: Float = 0.2f,
    private val maxLostDurationMs: Long = 800L
) {
    private var lastValidBounds: TorsoBounds? = null
    private var trackingLostTimeMs: Long = 0L
    private var isTracking = false

    private var smoothedCenter: Offset? = null
    private var smoothedShoulderWidth: Float? = null
    private var smoothedTorsoWidth: Float? = null
    private var smoothedTorsoHeight: Float? = null
    private var smoothedRotation: Float? = null

    fun track(landmarks: List<PoseResult.PoseLandmark>, timestampMs: Long): GarmentAnchorPoints {
        val leftShoulder = landmarks.firstOrNull { it.id == 11 }
        val rightShoulder = landmarks.firstOrNull { it.id == 12 }
        val leftHip = landmarks.firstOrNull { it.id == 23 }
        val rightHip = landmarks.firstOrNull { it.id == 24 }

        val hasShoulders = leftShoulder != null && rightShoulder != null && 
                leftShoulder.likelihood >= confidenceThreshold && rightShoulder.likelihood >= confidenceThreshold
        val hasHips = leftHip != null && rightHip != null && 
                leftHip.likelihood >= confidenceThreshold && rightHip.likelihood >= confidenceThreshold

        if (hasShoulders && hasHips) {
            val ls = Offset(leftShoulder!!.x, leftShoulder.y)
            val rs = Offset(rightShoulder!!.x, rightShoulder.y)
            val lh = Offset(leftHip!!.x, leftHip.y)
            val rh = Offset(rightHip!!.x, rightHip.y)

            val center = Offset(
                (ls.x + rs.x + lh.x + rh.x) / 4f,
                (ls.y + rs.y + lh.y + rh.y) / 4f
            )

            val sWidth = getDistance(ls, rs)
            val hWidth = getDistance(lh, rh)
            val tWidth = (sWidth + hWidth) / 2f
            
            val leftHeight = getDistance(lh, ls)
            val rightHeight = getDistance(rh, rs)
            val tHeight = (leftHeight + rightHeight) / 2f

            val dx = rs.x - ls.x
            val dy = rs.y - ls.y
            val rollRad = atan2(dy, dx)
            val rollDeg = Math.toDegrees(rollRad.toDouble()).toFloat()

            val confidence = (leftShoulder.likelihood + rightShoulder.likelihood + leftHip.likelihood + rightHip.likelihood) / 4f

            // Smooth position
            val sc = smoothedCenter
            val newCenter = if (sc == null) center else sc + (center - sc) * smoothingFactor
            smoothedCenter = newCenter

            // Smooth scale
            val ssw = smoothedShoulderWidth
            val newSWidth = if (ssw == null) sWidth else ssw + (sWidth - ssw) * smoothingFactor
            smoothedShoulderWidth = newSWidth

            val stw = smoothedTorsoWidth
            val newTWidth = if (stw == null) tWidth else stw + (tWidth - stw) * smoothingFactor
            smoothedTorsoWidth = newTWidth

            val sth = smoothedTorsoHeight
            val newHeight = if (sth == null) tHeight else sth + (tHeight - sth) * smoothingFactor
            smoothedTorsoHeight = newHeight

            // Smooth rotation (angle wrapping)
            val sr = smoothedRotation
            val newRotation = if (sr == null) rollDeg else {
                val diff = rollDeg - sr
                val normalizedDiff = atan2(sin(Math.toRadians(diff.toDouble())), cos(Math.toRadians(diff.toDouble())))
                sr + Math.toDegrees(normalizedDiff).toFloat() * smoothingFactor
            }
            smoothedRotation = newRotation

            val newBounds = TorsoBounds(
                leftShoulder = ls,
                rightShoulder = rs,
                leftHip = lh,
                rightHip = rh,
                center = newCenter,
                shoulderWidth = newSWidth,
                torsoWidth = newTWidth,
                torsoHeight = newHeight,
                rotation = newRotation,
                confidence = confidence
            )

            lastValidBounds = newBounds
            trackingLostTimeMs = 0L
            isTracking = true

            return GarmentAnchorPoints(
                bounds = newBounds,
                isTrackingActive = true,
                trackingLostTimeMs = 0L
            )
        } else {
            if (isTracking) {
                if (trackingLostTimeMs == 0L) {
                    trackingLostTimeMs = timestampMs
                }
                
                val elapsedLoss = timestampMs - trackingLostTimeMs
                if (elapsedLoss < maxLostDurationMs && lastValidBounds != null) {
                    return GarmentAnchorPoints(
                        bounds = lastValidBounds!!,
                        isTrackingActive = true,
                        trackingLostTimeMs = elapsedLoss
                    )
                } else {
                    isTracking = false
                    reset()
                }
            }

            return GarmentAnchorPoints(
                bounds = TorsoBounds(
                    leftShoulder = Offset.Zero,
                    rightShoulder = Offset.Zero,
                    leftHip = Offset.Zero,
                    rightHip = Offset.Zero,
                    center = Offset.Zero,
                    shoulderWidth = 0f,
                    torsoWidth = 0f,
                    torsoHeight = 0f,
                    rotation = 0f,
                    confidence = 0f
                ),
                isTrackingActive = false,
                trackingLostTimeMs = timestampMs - trackingLostTimeMs
            )
        }
    }

    private fun getDistance(p1: Offset, p2: Offset): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun reset() {
        lastValidBounds = null
        trackingLostTimeMs = 0L
        isTracking = false
        smoothedCenter = null
        smoothedShoulderWidth = null
        smoothedTorsoWidth = null
        smoothedTorsoHeight = null
        smoothedRotation = null
    }
}
