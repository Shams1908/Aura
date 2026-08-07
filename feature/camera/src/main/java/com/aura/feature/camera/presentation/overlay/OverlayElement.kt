package com.aura.feature.camera.presentation.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface OverlayElement {
    val id: String
    val layer: OverlayLayer
    val style: OverlayStyle
    val isVisible: Boolean

    @Composable
    fun Render(modifier: Modifier, animator: OverlayAnimator)
}
