package com.aura.feature.camera.presentation.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun OverlayRenderer(
    viewModel: OverlayRendererViewModel,
    isDebugMode: Boolean,
    modifier: Modifier = Modifier
) {
    val state by viewModel.overlayState.collectAsState()
    val animator = remember { DefaultOverlayAnimator() }

    if (!state.isVisible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "AI Assistant Guidelines Overlay"
            }
    ) {
        val activeElements = state.elements
            .filter { it.isVisible && state.activeLayers.contains(it.layer) }
            .filter { element ->
                if (!isDebugMode) {
                    element.id != "pose_skeleton" &&
                    element.id != "face_guide" &&
                    element.id != "shoulder_guide" &&
                    element.id != "tracking_box" &&
                    element.id != "style_score_bubble" &&
                    element.id != "ai_status_bubble" &&
                    element.id != "recommendation_bubble" &&
                    !element.id.startsWith("garment_")
                } else {
                    true
                }
            }
            .sortedBy { it.layer.zIndex }

        activeElements.forEach { element ->
            key(element.id) {
                element.Render(
                    modifier = Modifier.fillMaxSize(),
                    animator = animator
                )
            }
        }
    }
}
