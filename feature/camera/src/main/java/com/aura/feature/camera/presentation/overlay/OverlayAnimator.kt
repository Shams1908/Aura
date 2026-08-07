package com.aura.feature.camera.presentation.overlay

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset

interface OverlayAnimator {
    @Composable
    fun pulse(
        initialValue: Float,
        targetValue: Float,
        durationMillis: Int
    ): State<Float>

    @Composable
    fun fade(
        targetValue: Float,
        durationMillis: Int
    ): State<Float>

    @Composable
    fun scale(
        targetValue: Float,
        durationMillis: Int
    ): State<Float>

    @Composable
    fun slide(
        targetOffset: Offset,
        durationMillis: Int
    ): State<Offset>

    @Composable
    fun tracking(
        targetOffset: Offset,
        dampingRatio: Float,
        stiffness: Float
    ): State<Offset>
}

@Composable
fun OverlayAnimator.pulse(
    initialValue: Float = 0.35f,
    targetValue: Float = 0.85f,
    durationMillis: Int = 1800
): State<Float> = pulse(initialValue, targetValue, durationMillis)

@Composable
fun OverlayAnimator.fade(
    targetValue: Float,
    durationMillis: Int = 300
): State<Float> = fade(targetValue, durationMillis)

@Composable
fun OverlayAnimator.scale(
    targetValue: Float,
    durationMillis: Int = 300
): State<Float> = scale(targetValue, durationMillis)

@Composable
fun OverlayAnimator.slide(
    targetOffset: Offset,
    durationMillis: Int = 400
): State<Offset> = slide(targetOffset, durationMillis)

@Composable
fun OverlayAnimator.tracking(
    targetOffset: Offset,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessLow
): State<Offset> = tracking(targetOffset, dampingRatio, stiffness)

class DefaultOverlayAnimator : OverlayAnimator {
    @Composable
    override fun pulse(
        initialValue: Float,
        targetValue: Float,
        durationMillis: Int
    ): State<Float> {
        val transition = rememberInfiniteTransition(label = "pulse")
        return transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    }

    @Composable
    override fun fade(
        targetValue: Float,
        durationMillis: Int
    ): State<Float> {
        return animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(durationMillis, easing = LinearOutSlowInEasing),
            label = "fade"
        )
    }

    @Composable
    override fun scale(
        targetValue: Float,
        durationMillis: Int
    ): State<Float> {
        return animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
            label = "scale"
        )
    }

    @Composable
    override fun slide(
        targetOffset: Offset,
        durationMillis: Int
    ): State<Offset> {
        return animateOffsetAsState(
            targetValue = targetOffset,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
            label = "slide"
        )
    }

    @Composable
    override fun tracking(
        targetOffset: Offset,
        dampingRatio: Float,
        stiffness: Float
    ): State<Offset> {
        return animateOffsetAsState(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness),
            label = "tracking"
        )
    }
}
