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
import com.aura.core.vision.model.PoseResult
import com.aura.feature.camera.domain.GarmentAnchorPoints

fun transformCoordinates(
    x_s: Float,
    y_s: Float,
    W_s: Float,
    H_s: Float,
    rotation: Int,
    isFront: Boolean,
    W_v: Float,
    H_v: Float
): Offset {
    // 1. Rotate sensor coordinates to rotated coordinates
    val (x_r, y_r) = when (rotation) {
        90 -> Pair(H_s - y_s, x_s)
        180 -> Pair(W_s - x_s, H_s - y_s)
        270 -> Pair(y_s, W_s - x_s)
        else -> Pair(x_s, y_s)
    }

    // Rotated dimensions
    val W_r = if (rotation == 90 || rotation == 270) H_s else W_s
    val H_r = if (rotation == 90 || rotation == 270) W_s else H_s

    // 2. Scale and offset to match FILL_CENTER in view
    val scaleX = W_v / W_r
    val scaleY = H_v / H_r
    val scale = maxOf(scaleX, scaleY)

    val offsetX = (W_v - W_r * scale) / 2f
    val offsetY = (H_v - H_r * scale) / 2f

    val x_v = x_r * scale + offsetX
    val y_v = y_r * scale + offsetY

    // 3. Handle front camera mirroring
    val finalX = if (isFront) W_v - x_v else x_v
    val finalY = y_v

    return Offset(finalX, finalY)
}

fun transformTrackedTransform(
    raw: TrackedTransform,
    pose: PoseResult,
    viewWidth: Float,
    viewHeight: Float
): TrackedTransform {
    val mappedCenter = transformCoordinates(
        x_s = raw.centerX,
        y_s = raw.centerY,
        W_s = pose.imageWidth.toFloat(),
        H_s = pose.imageHeight.toFloat(),
        rotation = pose.rotationDegrees,
        isFront = pose.isFrontCamera,
        W_v = viewWidth,
        H_v = viewHeight
    )

    val rotation = pose.rotationDegrees
    val W_s = pose.imageWidth.toFloat()
    val H_s = pose.imageHeight.toFloat()
    val W_r = if (rotation == 90 || rotation == 270) H_s else W_s
    val H_r = if (rotation == 90 || rotation == 270) W_s else H_s

    val scaleX = viewWidth / W_r
    val scaleY = viewHeight / H_r
    val scale = maxOf(scaleX, scaleY)

    val mappedWidth = raw.width * scale
    val mappedHeight = raw.height * scale
    val mappedRotation = if (pose.isFrontCamera) -raw.rotation else raw.rotation

    return TrackedTransform(
        centerX = mappedCenter.x,
        centerY = mappedCenter.y,
        width = mappedWidth,
        height = mappedHeight,
        rotation = mappedRotation,
        confidence = raw.confidence,
        isTrackingActive = raw.isTrackingActive
    )
}

data class PoseSkeletonElement(
    val pose: PoseResult,
    override val id: String = "pose_skeleton",
    override val layer: OverlayLayer = OverlayLayer.BODY,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true
) : OverlayElement {

    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val landmarks = pose.landmarks
            if (landmarks.isEmpty()) return@Canvas

            // Map landmark points using transformCoordinates
            val pointsMap = landmarks.associate { landmark ->
                val screenOffset = transformCoordinates(
                    x_s = landmark.x,
                    y_s = landmark.y,
                    W_s = pose.imageWidth.toFloat(),
                    H_s = pose.imageHeight.toFloat(),
                    rotation = pose.rotationDegrees,
                    isFront = pose.isFrontCamera,
                    W_v = width,
                    H_v = height
                )
                landmark.id to screenOffset
            }

            // Extract needed points
            val leftShoulder = pointsMap[11]
            val rightShoulder = pointsMap[12]
            val leftElbow = pointsMap[13]
            val rightElbow = pointsMap[14]
            val leftWrist = pointsMap[15]
            val rightWrist = pointsMap[16]
            val leftHip = pointsMap[23]
            val rightHip = pointsMap[24]

            val strokeWidth = 3.dp.toPx()

            // 1. Torso Boundary
            if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
                val torsoPath = Path().apply {
                    moveTo(leftShoulder.x, leftShoulder.y)
                    lineTo(rightShoulder.x, rightShoulder.y)
                    lineTo(rightHip.x, rightHip.y)
                    lineTo(leftHip.x, leftHip.y)
                    close()
                }
                drawPath(
                    path = torsoPath,
                    color = style.primaryColor.copy(alpha = 0.2f)
                )
                drawPath(
                    path = torsoPath,
                    color = style.primaryColor,
                    style = Stroke(width = strokeWidth)
                )
            }

            // 2. Arms
            if (leftShoulder != null && leftElbow != null) {
                drawLine(color = style.primaryColor, start = leftShoulder, end = leftElbow, strokeWidth = strokeWidth)
            }
            if (leftElbow != null && leftWrist != null) {
                drawLine(color = style.primaryColor, start = leftElbow, end = leftWrist, strokeWidth = strokeWidth)
            }
            if (rightShoulder != null && rightElbow != null) {
                drawLine(color = style.primaryColor, start = rightShoulder, end = rightElbow, strokeWidth = strokeWidth)
            }
            if (rightElbow != null && rightWrist != null) {
                drawLine(color = style.primaryColor, start = rightElbow, end = rightWrist, strokeWidth = strokeWidth)
            }

            // 3. Joints (Draw glowing circles)
            val jointsColor = Color.White
            pointsMap.values.forEach { point ->
                drawCircle(
                    color = jointsColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = style.primaryColor,
                    radius = 8.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

data class TorsoGuideElement(
    val anchorPoints: GarmentAnchorPoints,
    val pose: PoseResult,
    override val id: String = "torso_guide",
    override val layer: OverlayLayer = OverlayLayer.GARMENT,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFFFFCC00)),
    override val isVisible: Boolean = true
) : OverlayElement {

    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        if (!anchorPoints.isTrackingActive) return

        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val bounds = anchorPoints.bounds

            val transformedLeftShoulder = transformCoordinates(bounds.leftShoulder.x, bounds.leftShoulder.y, pose.imageWidth.toFloat(), pose.imageHeight.toFloat(), pose.rotationDegrees, pose.isFrontCamera, width, height)
            val transformedRightShoulder = transformCoordinates(bounds.rightShoulder.x, bounds.rightShoulder.y, pose.imageWidth.toFloat(), pose.imageHeight.toFloat(), pose.rotationDegrees, pose.isFrontCamera, width, height)
            val transformedLeftHip = transformCoordinates(bounds.leftHip.x, bounds.leftHip.y, pose.imageWidth.toFloat(), pose.imageHeight.toFloat(), pose.rotationDegrees, pose.isFrontCamera, width, height)
            val transformedRightHip = transformCoordinates(bounds.rightHip.x, bounds.rightHip.y, pose.imageWidth.toFloat(), pose.imageHeight.toFloat(), pose.rotationDegrees, pose.isFrontCamera, width, height)
            val transformedCenter = transformCoordinates(bounds.center.x, bounds.center.y, pose.imageWidth.toFloat(), pose.imageHeight.toFloat(), pose.rotationDegrees, pose.isFrontCamera, width, height)

            val path = Path().apply {
                moveTo(transformedLeftShoulder.x, transformedLeftShoulder.y)
                lineTo(transformedRightShoulder.x, transformedRightShoulder.y)
                lineTo(transformedRightHip.x, transformedRightHip.y)
                lineTo(transformedLeftHip.x, transformedLeftHip.y)
                close()
            }

            val opacity = if (bounds.confidence < 0.6f) 0.15f else 0.3f

            drawPath(
                path = path,
                color = style.primaryColor.copy(alpha = opacity)
            )

            val strokeWidth = 3.dp.toPx()
            drawPath(
                path = path,
                color = style.primaryColor,
                style = Stroke(width = strokeWidth)
            )

            val crosshairSize = 15.dp.toPx()
            drawLine(
                color = style.primaryColor,
                start = Offset(transformedCenter.x - crosshairSize, transformedCenter.y),
                end = Offset(transformedCenter.x + crosshairSize, transformedCenter.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = style.primaryColor,
                start = Offset(transformedCenter.x, transformedCenter.y - crosshairSize),
                end = Offset(transformedCenter.x, transformedCenter.y + crosshairSize),
                strokeWidth = 2.dp.toPx()
            )

            val anchorColor = Color.White
            drawCircle(color = anchorColor, radius = 5.dp.toPx(), center = transformedLeftShoulder)
            drawCircle(color = anchorColor, radius = 5.dp.toPx(), center = transformedRightShoulder)
            drawCircle(color = anchorColor, radius = 5.dp.toPx(), center = transformedLeftHip)
            drawCircle(color = anchorColor, radius = 5.dp.toPx(), center = transformedRightHip)
        }
    }
}

data class TrackedTorsoBoxElement(
    val transform: TrackedTransform,
    val pose: PoseResult,
    override val id: String = "tracked_torso_box",
    override val layer: OverlayLayer = OverlayLayer.GARMENT,
    override val style: OverlayStyle = OverlayStyle(primaryColor = Color(0xFF00E5FF)),
    override val isVisible: Boolean = true
) : OverlayElement {

    @Composable
    override fun Render(modifier: Modifier, animator: OverlayAnimator) {
        if (!transform.isTrackingActive || transform.confidence < 0.5f) return

        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val mapped = transformTrackedTransform(transform, pose, width, height)

            val boxWidth = mapped.width
            val boxHeight = mapped.height
            val centerX = mapped.centerX
            val centerY = mapped.centerY
            val rotationDegrees = mapped.rotation

            drawContext.canvas.save()
            drawContext.transform.rotate(degrees = rotationDegrees, pivot = Offset(centerX, centerY))

            val topLeft = Offset(centerX - boxWidth / 2f, centerY - boxHeight / 2f)
            val strokeWidth = 2.dp.toPx()
            val bracketLen = minOf(boxWidth, boxHeight) * 0.15f
            val color = style.primaryColor.copy(alpha = 0.8f)

            // Top-left
            drawLine(color, topLeft, Offset(topLeft.x + bracketLen, topLeft.y), strokeWidth)
            drawLine(color, topLeft, Offset(topLeft.x, topLeft.y + bracketLen), strokeWidth)

            // Top-right
            val topRight = Offset(topLeft.x + boxWidth, topLeft.y)
            drawLine(color, topRight, Offset(topRight.x - bracketLen, topRight.y), strokeWidth)
            drawLine(color, topRight, Offset(topRight.x, topRight.y + bracketLen), strokeWidth)

            // Bottom-left
            val bottomLeft = Offset(topLeft.x, topLeft.y + boxHeight)
            drawLine(color, bottomLeft, Offset(bottomLeft.x + bracketLen, bottomLeft.y), strokeWidth)
            drawLine(color, bottomLeft, Offset(bottomLeft.x, bottomLeft.y - bracketLen), strokeWidth)

            // Bottom-right
            val bottomRight = Offset(topLeft.x + boxWidth, topLeft.y + boxHeight)
            drawLine(color, bottomRight, Offset(bottomRight.x - bracketLen, bottomRight.y), strokeWidth)
            drawLine(color, bottomRight, Offset(bottomRight.x, bottomRight.y - bracketLen), strokeWidth)

            // Crosshair
            val crossSize = 10.dp.toPx()
            drawLine(color, Offset(centerX - crossSize, centerY), Offset(centerX + crossSize, centerY), 1.5.dp.toPx())
            drawLine(color, Offset(centerX, centerY - crossSize), Offset(centerX, centerY + crossSize), 1.5.dp.toPx())

            // Semi-transparent center box fill
            drawRect(
                color = style.primaryColor.copy(alpha = 0.08f),
                topLeft = topLeft,
                size = Size(boxWidth, boxHeight)
            )

            drawContext.canvas.restore()
        }
    }
}

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
