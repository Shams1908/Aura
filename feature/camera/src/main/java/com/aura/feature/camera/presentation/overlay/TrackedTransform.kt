package com.aura.feature.camera.presentation.overlay

data class TrackedTransform(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val rotation: Float, // roll in degrees
    val confidence: Float,
    val isTrackingActive: Boolean
)
