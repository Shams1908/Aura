package com.aura.feature.camera.presentation.overlay

data class OverlayState(
    val elements: List<OverlayElement> = emptyList(),
    val activeLayers: Set<OverlayLayer> = OverlayLayer.values().toSet(),
    val isVisible: Boolean = true
)
