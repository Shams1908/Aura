package com.aura.feature.camera.domain

import androidx.compose.ui.geometry.Offset

enum class PlacementType {
    UPPER, LOWER, DRESS
}

data class BodyAnchor(
    val leftShoulder: Offset,
    val rightShoulder: Offset,
    val leftHip: Offset,
    val rightHip: Offset,
    val center: Offset,
    val shoulderWidth: Float,
    val torsoWidth: Float,
    val torsoHeight: Float,
    val rotation: Float,
    val confidence: Float,
    val isTrackingActive: Boolean
)
