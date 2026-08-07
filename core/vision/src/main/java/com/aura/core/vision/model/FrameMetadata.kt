package com.aura.core.vision.model

data class FrameMetadata(
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val timestampMs: Long,
    val lensFacing: Int
)
