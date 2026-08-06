package com.aura.feature.camera.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Transparent canvas showing posture guidelines (face alignment, shoulder lines, full-body outline).
 * Houses placeholders for future AI features (Pose Skeleton, Garment Mesh, Style Score, etc.).
 */
@Composable
fun StudioOverlay(
    modifier: Modifier = Modifier
) {
    // Pulse animation for alignment guides
    val infiniteTransition = rememberInfiniteTransition(label = "Overlay Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha Pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Transparent alignment guide shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val guideColor = Color(0xFF00E5FF).copy(alpha = pulseAlpha)
            val outerFrameColor = Color.White.copy(alpha = 0.15f)
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)

            // Full body crop frame guide
            val frameWidth = width * 0.8f
            val frameHeight = height * 0.72f
            val frameLeft = (width - frameWidth) / 2
            val frameTop = (height - frameHeight) / 2 - 20.dp.toPx()

            drawRect(
                color = outerFrameColor,
                topLeft = Offset(frameLeft, frameTop),
                size = Size(frameWidth, frameHeight),
                style = Stroke(width = 1.5f, pathEffect = dashPathEffect)
            )

            // Face guide oval
            val faceWidth = width * 0.3f
            val faceHeight = height * 0.15f
            val faceLeft = (width - faceWidth) / 2
            val faceTop = height * 0.15f

            drawOval(
                color = guideColor,
                topLeft = Offset(faceLeft, faceTop),
                size = Size(faceWidth, faceHeight),
                style = Stroke(width = 3f)
            )

            // Shoulder guide paths
            val shoulderPath = Path().apply {
                val startX = frameLeft + 15.dp.toPx()
                val startY = height * 0.38f
                val endX = startX + frameWidth - 30.dp.toPx()
                val endY = height * 0.38f

                moveTo(startX, startY)
                cubicTo(
                    width * 0.32f, height * 0.32f,
                    width * 0.42f, height * 0.32f,
                    width * 0.5f, height * 0.32f
                )
                cubicTo(
                    width * 0.58f, height * 0.32f,
                    width * 0.68f, height * 0.32f,
                    endX, endY
                )
            }

            drawPath(
                path = shoulderPath,
                color = guideColor,
                style = Stroke(width = 3f)
            )

            // Corner guide tick marks around the face oval
            val tickSize = 12.dp.toPx()
            val tickWidth = 3f

            // Top Left
            drawLine(guideColor, Offset(faceLeft, faceTop), Offset(faceLeft + tickSize, faceTop), strokeWidth = tickWidth)
            drawLine(guideColor, Offset(faceLeft, faceTop), Offset(faceLeft, faceTop + tickSize), strokeWidth = tickWidth)

            // Top Right
            drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth - tickSize, faceTop), strokeWidth = tickWidth)
            drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth, faceTop + tickSize), strokeWidth = tickWidth)

            // Bottom Left
            drawLine(guideColor, Offset(faceLeft, faceTop + faceHeight), Offset(faceLeft + tickSize, faceTop + faceHeight), strokeWidth = tickWidth)
            drawLine(guideColor, Offset(faceLeft, faceTop + faceHeight), Offset(faceLeft, faceTop + faceHeight - tickSize), strokeWidth = tickWidth)

            // Bottom Right
            drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop + faceHeight), Offset(faceLeft + faceWidth - tickSize, faceTop + faceHeight), strokeWidth = tickWidth)
            drawLine(guideColor, Offset(faceLeft + faceWidth, faceTop + faceHeight), Offset(faceLeft + faceWidth, faceTop + faceHeight - tickSize), strokeWidth = tickWidth)
        }

        // 2. Future-Ready HUD Placeholder Overlays
        // Floating metrics panels styled like a futuristic cyberpunk AI interface
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HudPlaceholderBadge(label = "POSE SKELETON: STANDBY")
            HudPlaceholderBadge(label = "GARMENT MESH: DETECTING")
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 90.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HudPlaceholderBadge(label = "STYLE SCORE: --%")
            HudPlaceholderBadge(label = "TRACKING: AUTO")
        }

        // Bottom horizontal suggestion ticker placeholder
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 270.dp)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "AI Suggestion: Stand upright and align shoulders for overlay mapping...",
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Premium Sci-Fi HUD badge for future AI capabilities.
 */
@Composable
private fun HudPlaceholderBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
