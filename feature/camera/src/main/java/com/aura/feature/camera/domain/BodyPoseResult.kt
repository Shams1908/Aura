package com.aura.feature.camera.domain

data class BodyPoseResult(
    val landmarks: List<BodyLandmark>,
    val timestampMs: Long
)
