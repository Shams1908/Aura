package com.aura.feature.camera.domain

import com.aura.core.vision.model.PoseResult
import com.aura.feature.ai.domain.GarmentAssetProvider
import com.aura.feature.ai.model.GarmentAsset
import javax.inject.Inject
import javax.inject.Singleton

sealed interface VirtualTryOnRenderState {
    object NoAsset : VirtualTryOnRenderState
    object NoTracking : VirtualTryOnRenderState
    data class UnsupportedCategory(val category: String) : VirtualTryOnRenderState
    data class Render(
        val asset: GarmentAsset,
        val transform: GarmentTransformResult.Success
    ) : VirtualTryOnRenderState
}

@Singleton
class VirtualTryOnController @Inject constructor(
    private val garmentAssetProvider: GarmentAssetProvider,
    private val bodyAnchorProvider: BodyAnchorProvider,
    private val transformCalculator: GarmentTransformCalculator
) {
    private var smoothedTransform: GarmentTransformResult.Success? = null
    private var lastIsFrontCamera: Boolean? = null
    
    // Low-pass filter smoothing factor (e.g. 0.2f for responsive but stable movement)
    private val smoothingFactor = 0.20f
    private val positionJumpThreshold = 150f // pixels

    fun processFrame(
        pose: PoseResult?,
        timestampMs: Long,
        viewWidth: Float,
        viewHeight: Float
    ): VirtualTryOnRenderState {
        val asset = garmentAssetProvider.getGarmentAsset()
            ?: run {
                resetSmoothing()
                return VirtualTryOnRenderState.NoAsset
            }
            
        // Reset smoothing if camera switched front/back
        val isFront = pose?.isFrontCamera ?: false
        if (lastIsFrontCamera != null && lastIsFrontCamera != isFront) {
            resetSmoothing()
        }
        lastIsFrontCamera = isFront

        val anchor = bodyAnchorProvider.getAnchor(pose, timestampMs)
            ?: run {
                resetSmoothing()
                return VirtualTryOnRenderState.NoTracking
            }
            
        val transformResult = transformCalculator.calculateTransform(
            asset = asset,
            anchor = anchor,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            pose = pose
        )
        
        return when (transformResult) {
            is GarmentTransformResult.UnsupportedGarmentCategory -> {
                resetSmoothing()
                VirtualTryOnRenderState.UnsupportedCategory(asset.category)
            }
            is GarmentTransformResult.InvalidAnchor -> {
                resetSmoothing()
                VirtualTryOnRenderState.NoTracking
            }
            is GarmentTransformResult.Success -> {
                val smoothed = smoothTransform(transformResult)
                VirtualTryOnRenderState.Render(asset, smoothed)
            }
        }
    }

    fun reset() {
        bodyAnchorProvider.reset()
        resetSmoothing()
        lastIsFrontCamera = null
    }

    fun resetSmoothing() {
        smoothedTransform = null
    }

    private fun smoothTransform(newVal: GarmentTransformResult.Success): GarmentTransformResult.Success {
        val prev = smoothedTransform
        if (prev == null) {
            smoothedTransform = newVal
            return newVal
        }
        
        // Reset smoothing if the position jumps an unrealistic distance
        val dx = newVal.translationX - prev.translationX
        val dy = newVal.translationY - prev.translationY
        val distance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (distance > positionJumpThreshold) {
            smoothedTransform = newVal
            return newVal
        }
        
        // Exponential Moving Average: smoothed = prev + alpha * (newVal - prev)
        val smoothedX = prev.translationX + smoothingFactor * (newVal.translationX - prev.translationX)
        val smoothedY = prev.translationY + smoothingFactor * (newVal.translationY - prev.translationY)
        val smoothedW = prev.width + smoothingFactor * (newVal.width - prev.width)
        val smoothedH = prev.height + smoothingFactor * (newVal.height - prev.height)
        
        // Smooth rotation using shortest path interpolation (angle wrapping)
        val diffRot = newVal.rotation - prev.rotation
        val normalizedDiff = kotlin.math.atan2(
            kotlin.math.sin(Math.toRadians(diffRot.toDouble())),
            kotlin.math.cos(Math.toRadians(diffRot.toDouble()))
        )
        val smoothedRot = prev.rotation + smoothingFactor * Math.toDegrees(normalizedDiff).toFloat()
        
        val result = GarmentTransformResult.Success(
            translationX = smoothedX,
            translationY = smoothedY,
            width = smoothedW,
            height = smoothedH,
            rotation = smoothedRot,
            scale = newVal.scale
        )
        smoothedTransform = result
        return result
    }
}
