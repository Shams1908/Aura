package com.aura.feature.camera.presentation.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.aura.core.vision.model.PoseResult
import com.aura.feature.camera.domain.VirtualTryOnController
import com.aura.feature.camera.domain.VirtualTryOnRenderState

/**
 * OverlayElement that computes and renders the transparent garment asset texture
 * over the tracked person's body using the VirtualTryOnController state.
 */
data class GarmentOverlayRendererElement(
    val pose: PoseResult?,
    val timestampMs: Long,
    val virtualTryOnController: VirtualTryOnController,
    override val id: String = "garment_overlay_renderer",
    override val layer: OverlayLayer = OverlayLayer.GARMENT,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color.Transparent),
    override val isVisible: Boolean = true
) : OverlayElement {
    override val purpose: OverlayPurpose = OverlayPurpose.PRODUCTION

    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Calculate transform dynamically using view size
            val renderState = virtualTryOnController.processFrame(
                pose = pose,
                timestampMs = timestampMs,
                viewWidth = width,
                viewHeight = height
            )

            // Logging for debug builds only
            if (com.aura.feature.camera.BuildConfig.DEBUG) {
                when (renderState) {
                    is VirtualTryOnRenderState.Render -> {
                        val asset = renderState.asset
                        val t = renderState.transform
                        android.util.Log.d(
                            "VirtualTryOn",
                            "Category: ${asset.category}, " +
                            "Anchor dimensions: (w=${t.width}, h=${t.height}), " +
                            "Smoothed transform: (x=${t.translationX}, y=${t.translationY}, rot=${t.rotation}), " +
                            "Render active: true"
                        )
                    }
                    is VirtualTryOnRenderState.UnsupportedCategory -> {
                        android.util.Log.d(
                            "VirtualTryOn",
                            "Unsupported category: ${renderState.category}, Render active: false"
                        )
                    }
                    else -> {
                        // Throttled or simple log for tracking loss / no asset
                        android.util.Log.d("VirtualTryOn", "Render active: false, State: $renderState")
                    }
                }
            }

            // Perform transparent drawing on Canvas if state is Render
            if (renderState is VirtualTryOnRenderState.Render) {
                val asset = renderState.asset
                val t = renderState.transform
                val textureBitmap = asset.texture.asImageBitmap()

                drawContext.canvas.save()
                try {
                    val halfW = t.width / 2f
                    val halfH = t.height / 2f

                    // Translate to the chest/waist center and rotate around it
                    drawContext.transform.translate(t.translationX, t.translationY)
                    drawContext.transform.rotate(t.rotation)

                    // Draw image centered on (0,0) in transformed space
                    drawImage(
                        image = textureBitmap,
                        dstOffset = IntOffset(-halfW.toInt(), -halfH.toInt()),
                        dstSize = IntSize(t.width.toInt(), t.height.toInt())
                    )
                } finally {
                    drawContext.canvas.restore()
                }
            }
        }
    }
}
