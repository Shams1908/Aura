package com.aura.feature.camera.domain

import com.aura.core.vision.model.PoseResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provider responsible for wrapping the torso tracking algorithms and supplying a stable BodyAnchor.
 */
@Singleton
class BodyAnchorProvider @Inject constructor() {
    private val torsoTracker = TorsoTracker()

    fun getAnchor(pose: PoseResult?, timestampMs: Long): BodyAnchor? {
        val landmarks = pose?.landmarks ?: emptyList()
        val anchors = torsoTracker.track(landmarks, timestampMs)
        return if (anchors.isTrackingActive) {
            BodyAnchor(
                leftShoulder = anchors.bounds.leftShoulder,
                rightShoulder = anchors.bounds.rightShoulder,
                leftHip = anchors.bounds.leftHip,
                rightHip = anchors.bounds.rightHip,
                center = anchors.bounds.center,
                shoulderWidth = anchors.bounds.shoulderWidth,
                torsoWidth = anchors.bounds.torsoWidth,
                torsoHeight = anchors.bounds.torsoHeight,
                rotation = anchors.bounds.rotation,
                confidence = anchors.bounds.confidence,
                isTrackingActive = anchors.isTrackingActive
            )
        } else {
            null
        }
    }

    fun reset() {
        torsoTracker.reset()
    }
}
