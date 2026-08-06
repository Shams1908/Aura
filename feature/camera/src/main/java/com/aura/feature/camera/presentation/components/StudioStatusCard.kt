package com.aura.feature.camera.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.feature.camera.presentation.StudioStatus

/**
 * Top Status Card that presents mock status updates.
 * Features a glowing glassmorphic pill that cycles statuses on click.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StudioStatusCard(
    status: StudioStatus,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for status indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "Status Pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot Alpha"
    )

    // Dynamic colors based on active status
    val (statusText, accentColor, iconColor) = when (status) {
        StudioStatus.CAMERA_READY -> Triple("Camera Ready", Color(0xFF00E676), Color(0xFF00E676))
        StudioStatus.TRACKING_WAITING -> Triple("Tracking Waiting", Color(0xFFFFAB40), Color(0xFFFFAB40))
        StudioStatus.OUTFIT_LOADED -> Triple("Outfit Loaded", Color(0xFF00E5FF), Color(0xFF00E5FF))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onStatusClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Dot Indicator with alpha pulse
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        iconColor.copy(
                            alpha = if (status == StudioStatus.TRACKING_WAITING) dotAlpha else 1.0f
                        )
                    )
            )

            // Animated content to transition text cleanly
            AnimatedContent(
                targetState = statusText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                },
                label = "Status Text Crossfade"
            ) { targetText ->
                Text(
                    text = targetText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
