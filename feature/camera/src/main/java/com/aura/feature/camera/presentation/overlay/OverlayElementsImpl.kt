package com.aura.feature.camera.presentation.overlay

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BodyOutlineElement(
    override val id: String = "body_outline",
    override val layer: OverlayLayer = OverlayLayer.BODY,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF).copy(alpha = 0.5f)),
    override val isVisible: Boolean = true
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        val pulseAlpha by animator.pulse(0.2f, 0.6f)
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(width * 0.5f, height * 0.15f)
                quadraticBezierTo(width * 0.45f, height * 0.2f, width * 0.42f, height * 0.28f)
                lineTo(width * 0.35f, height * 0.35f)
                lineTo(width * 0.38f, height * 0.6f)
                lineTo(width * 0.42f, height * 0.85f)
                lineTo(width * 0.58f, height * 0.85f)
                lineTo(width * 0.62f, height * 0.6f)
                lineTo(width * 0.65f, height * 0.35f)
                lineTo(width * 0.58f, height * 0.28f)
                quadraticBezierTo(width * 0.55f, height * 0.2f, width * 0.5f, height * 0.15f)
            }
            drawPath(
                path = path,
                color = style.primaryColor.copy(alpha = pulseAlpha),
                style = Stroke(width = style.strokeWidth.toPx())
            )
        }
    }
}

data class FaceGuideElement(
    override val id: String = "face_guide",
    override val layer: OverlayLayer = OverlayLayer.BODY,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        val pulseAlpha by animator.pulse(0.4f, 0.9f)
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val faceWidth = width * 0.3f
            val faceHeight = height * 0.15f
            val faceLeft = (width - faceWidth) / 2
            val faceTop = height * 0.15f

            drawOval(
                color = style.primaryColor.copy(alpha = pulseAlpha),
                topLeft = Offset(faceLeft, faceTop),
                size = Size(faceWidth, faceHeight),
                style = Stroke(width = style.strokeWidth.toPx() * 1.5f)
            )

            val tick = 12.dp.toPx()
            val sw = 3f
            val color = style.primaryColor.copy(alpha = pulseAlpha)
            drawLine(color, Offset(faceLeft, faceTop), Offset(faceLeft + tick, faceTop), sw)
            drawLine(color, Offset(faceLeft, faceTop), Offset(faceLeft, faceTop + tick), sw)
            drawLine(color, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth - tick, faceTop), sw)
            drawLine(color, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth, faceTop + tick), sw)
        }
    }
}

data class ShoulderGuideElement(
    override val id: String = "shoulder_guide",
    override val layer: OverlayLayer = OverlayLayer.BODY,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        val pulseAlpha by animator.pulse(0.4f, 0.9f)
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val startX = width * 0.25f
            val startY = height * 0.35f
            val endX = width * 0.75f
            val endY = height * 0.35f

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    width * 0.35f, height * 0.3f,
                    width * 0.45f, height * 0.3f,
                    width * 0.5f, height * 0.3f
                )
                cubicTo(
                    width * 0.55f, height * 0.3f,
                    width * 0.65f, height * 0.3f,
                    endX, endY
                )
            }

            drawPath(
                path = path,
                color = style.primaryColor.copy(alpha = pulseAlpha),
                style = Stroke(width = style.strokeWidth.toPx() * 1.5f)
            )
        }
    }
}

data class TrackingBoxElement(
    override val id: String = "tracking_box",
    override val layer: OverlayLayer = OverlayLayer.TRACKING,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFFFFFF00)),
    override val isVisible: Boolean = true,
    val targetOffset: Offset = Offset(200f, 400f),
    val targetSize: Size = Size(350f, 550f)
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        val animatedOffset by animator.tracking(targetOffset)
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRoundRect(
                color = style.primaryColor,
                topLeft = animatedOffset,
                size = targetSize,
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(
                    width = style.strokeWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            )
        }
    }
}

data class GarmentBoundingBoxElement(
    override val id: String,
    override val layer: OverlayLayer = OverlayLayer.GARMENT,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFFFF007F)),
    override val isVisible: Boolean = true,
    val label: String,
    val rectOffset: Offset,
    val rectSize: Size
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        val animatedOffset by animator.tracking(rectOffset)
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRoundRect(
                color = style.primaryColor,
                topLeft = animatedOffset,
                size = rectSize,
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = style.strokeWidth.toPx())
            )
        }
        val density = androidx.compose.ui.platform.LocalDensity.current
        val offsetDp = with(density) {
            IntOffset(animatedOffset.x.toInt(), (animatedOffset.y - 22.dp.toPx()).toInt())
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { offsetDp }
        ) {
            Text(
                text = label.uppercase(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .background(style.primaryColor, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

data class StyleScoreBubbleElement(
    override val id: String = "style_score_bubble",
    override val layer: OverlayLayer = OverlayLayer.INFORMATION,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true,
    val score: Int
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        val scale by animator.scale(1.0f)
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 90.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Card(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .border(0.5.dp, style.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column {
                        Text(
                            text = "STYLE SCORE",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$score%",
                            color = style.primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

data class AiStatusBubbleElement(
    override val id: String = "ai_status_bubble",
    override val layer: OverlayLayer = OverlayLayer.INFORMATION,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true,
    val statusMessage: String
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 90.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Card(
                modifier = Modifier.border(0.5.dp, style.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(style.primaryColor, RoundedCornerShape(50))
                    )
                    Column {
                        Text(
                            text = "PIPELINE ACTIVE",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = statusMessage.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

data class RecommendationBubbleElement(
    override val id: String = "recommendation_bubble",
    override val layer: OverlayLayer = OverlayLayer.INFORMATION,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true,
    val recommendations: List<String>
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 270.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (recommendations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, style.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AI RECOMMENDATIONS",
                            color = style.primaryColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        recommendations.forEach { recommendation ->
                            Text(
                                text = "➔ $recommendation",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ProgressIndicatorElement(
    override val id: String = "progress_indicator",
    override val layer: OverlayLayer = OverlayLayer.INFORMATION,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true,
    val progress: Float? = null
) : OverlayElement {
    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (progress != null) {
                        CircularProgressIndicator(
                            progress = progress,
                            color = style.primaryColor,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        CircularProgressIndicator(
                            color = style.primaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "SYNCING POSE MODEL",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
