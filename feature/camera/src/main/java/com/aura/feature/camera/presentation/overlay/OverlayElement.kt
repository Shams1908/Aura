package com.aura.feature.camera.presentation.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class OverlayPurpose {
    PRODUCTION,
    DEBUG,
    MOCK
}

interface OverlayElement {
    val id: String
    val layer: OverlayLayer
    val style: OverlayStyle
    val isVisible: Boolean
    val purpose: OverlayPurpose get() = OverlayPurpose.PRODUCTION

    @Composable
    fun Render(modifier: Modifier, animator: OverlayAnimator)
}
