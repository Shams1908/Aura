package com.aura.feature.camera.domain

data class BodyLandmark(
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float? = null,
    val likelihood: Float
)
