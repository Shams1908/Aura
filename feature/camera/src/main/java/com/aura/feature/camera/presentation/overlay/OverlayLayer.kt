package com.aura.feature.camera.presentation.overlay

enum class OverlayLayer(val zIndex: Float) {
    BACKGROUND(0f),
    CAMERA_PREVIEW(1f),
    BODY(2f),
    GARMENT(3f),
    TRACKING(4f),
    INFORMATION(5f),
    INTERACTION(6f)
}
