package com.aura.feature.camera.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Transparent canvas drawing posture guidelines (Face guidelines, shoulders curves, outer crop body frame).
 */
@Composable
fun CameraOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Tracking Pulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha Pulse"
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        
        val guideColor = Color(0xFF00E5FF).copy(alpha = pulseAlpha)
        val frameColor = Color.White.copy(alpha = 0.3f)
        
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)

        // 1. Draw Body Frame Guide Box
        val frameWidth = width * 0.75f
        val frameHeight = height * 0.8f
        val frameLeft = (width - frameWidth) / 2
        val frameTop = (height - frameHeight) / 2
        
        drawRect(
            color = frameColor,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            style = Stroke(width = 2f, pathEffect = dashPathEffect)
        )

        // 2. Draw Face Guide (centered top-mid oval)
        val faceWidth = width * 0.32f
        val faceHeight = height * 0.18f
        val faceLeft = (width - faceWidth) / 2
        val faceTop = height * 0.18f
        
        drawOval(
            color = guideColor,
            topLeft = Offset(faceLeft, faceTop),
            size = Size(faceWidth, faceHeight),
            style = Stroke(width = 3.5f)
        )

        // 3. Draw Shoulder Guides
        val shoulderPath = Path().apply {
            val startX = (width - frameWidth) / 2 + 10.dp.toPx()
            val startY = height * 0.44f
            val endX = startX + frameWidth - 20.dp.toPx()
            val endY = height * 0.44f
            
            moveTo(startX, startY)
            // Left curve
            cubicTo(
                width * 0.3f, height * 0.36f,
                width * 0.4f, height * 0.36f,
                width * 0.5f, height * 0.36f
            )
            // Right curve
            cubicTo(
                width * 0.6f, height * 0.36f,
                width * 0.7f, height * 0.36f,
                endX, endY
            )
        }
        
        drawPath(
            path = shoulderPath,
            color = guideColor,
            style = Stroke(width = 3.5f)
        )

        // 4. Draw Corner Tracking Guides around Face oval
        val cornerSize = 16.dp.toPx()
        
        // Top Left
        drawLine(guideColor, Offset(faceLeft, faceTop), Offset(faceLeft + cornerSize, faceTop), strokeWidth = 4f)
        drawLine(guideColor, Offset(faceLeft, faceTop), Offset(faceLeft, faceTop + cornerSize), strokeWidth = 4f)
        
        // Top Right
        drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth - cornerSize, faceTop), strokeWidth = 4f)
        drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth, faceTop + cornerSize), strokeWidth = 4f)
        
        // Bottom Left
        drawLine(guideColor, Offset(faceLeft, faceTop + faceHeight), Offset(faceLeft + cornerSize, faceTop + faceHeight), strokeWidth = 4f)
        drawLine(guideColor, Offset(faceLeft, faceTop + faceHeight), Offset(faceLeft, faceTop + faceHeight - cornerSize), strokeWidth = 4f)
        
        // Bottom Right
        drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop + faceHeight), Offset(faceLeft + faceWidth - cornerSize, faceTop + faceHeight), strokeWidth = 4f)
        drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop + faceHeight), Offset(faceLeft + faceWidth, faceTop + faceHeight - cornerSize), strokeWidth = 4f)
    }
}
