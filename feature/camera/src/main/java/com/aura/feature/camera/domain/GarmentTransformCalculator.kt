package com.aura.feature.camera.domain

import androidx.compose.ui.geometry.Offset
import com.aura.core.vision.model.PoseResult
import com.aura.feature.ai.model.GarmentAsset
import javax.inject.Inject
import javax.inject.Singleton

sealed interface GarmentTransformResult {
    data class Success(
        val translationX: Float,
        val translationY: Float,
        val width: Float,
        val height: Float,
        val rotation: Float,
        val scale: Float = 1f
    ) : GarmentTransformResult

    object UnsupportedGarmentCategory : GarmentTransformResult
    object InvalidAnchor : GarmentTransformResult
}

@Singleton
class GarmentTransformCalculator @Inject constructor() {

    fun getPlacementType(category: String): PlacementType? {
        val norm = category.lowercase().trim()
        return when {
            norm.contains("t-shirt") || norm.contains("tshirt") || norm.contains("t_shirt") -> PlacementType.UPPER
            norm.contains("shirt") || norm.contains("blouse") || norm.contains("hoodie") || 
            norm.contains("sweater") || norm.contains("cardigan") || norm.contains("top") -> PlacementType.UPPER
            norm.contains("jacket") || norm.contains("coat") || norm.contains("blazer") -> PlacementType.UPPER
            
            norm.contains("pants") || norm.contains("trousers") || norm.contains("jeans") || 
            norm.contains("leggings") || norm.contains("skirt") || norm.contains("shorts") -> PlacementType.LOWER
            
            norm.contains("dress") || norm.contains("jumpsuit") || norm.contains("romper") -> PlacementType.DRESS
            
            else -> null
        }
    }

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

    fun calculateTransform(
        asset: GarmentAsset,
        anchor: BodyAnchor,
        viewWidth: Float,
        viewHeight: Float,
        pose: PoseResult?
    ): GarmentTransformResult {
        if (pose == null || pose.imageWidth <= 0 || pose.imageHeight <= 0) {
            return GarmentTransformResult.InvalidAnchor
        }

        // 1. Resolve placement type based on category
        val placementType = getPlacementType(asset.category) 
            ?: return GarmentTransformResult.UnsupportedGarmentCategory

        // 2. Map anchor points from sensor space to view space
        val leftShoulderMapped = transformCoordinates(
            x_s = anchor.leftShoulder.x,
            y_s = anchor.leftShoulder.y,
            W_s = pose.imageWidth.toFloat(),
            H_s = pose.imageHeight.toFloat(),
            rotation = pose.rotationDegrees,
            isFront = pose.isFrontCamera,
            W_v = viewWidth,
            H_v = viewHeight
        )
        val rightShoulderMapped = transformCoordinates(
            x_s = anchor.rightShoulder.x,
            y_s = anchor.rightShoulder.y,
            W_s = pose.imageWidth.toFloat(),
            H_s = pose.imageHeight.toFloat(),
            rotation = pose.rotationDegrees,
            isFront = pose.isFrontCamera,
            W_v = viewWidth,
            H_v = viewHeight
        )
        val leftHipMapped = transformCoordinates(
            x_s = anchor.leftHip.x,
            y_s = anchor.leftHip.y,
            W_s = pose.imageWidth.toFloat(),
            H_s = pose.imageHeight.toFloat(),
            rotation = pose.rotationDegrees,
            isFront = pose.isFrontCamera,
            W_v = viewWidth,
            H_v = viewHeight
        )
        val rightHipMapped = transformCoordinates(
            x_s = anchor.rightHip.x,
            y_s = anchor.rightHip.y,
            W_s = pose.imageWidth.toFloat(),
            H_s = pose.imageHeight.toFloat(),
            rotation = pose.rotationDegrees,
            isFront = pose.isFrontCamera,
            W_v = viewWidth,
            H_v = viewHeight
        )

        // 3. Compute view-space properties
        val dxShoulder = rightShoulderMapped.x - leftShoulderMapped.x
        val dyShoulder = rightShoulderMapped.y - leftShoulderMapped.y
        val shoulderWidthMapped = kotlin.math.sqrt((dxShoulder * dxShoulder + dyShoulder * dyShoulder).toDouble()).toFloat()

        val dxHip = rightHipMapped.x - leftHipMapped.x
        val dyHip = rightHipMapped.y - leftHipMapped.y
        val hipWidthMapped = kotlin.math.sqrt((dxHip * dxHip + dyHip * dyHip).toDouble()).toFloat()

        if (shoulderWidthMapped <= 0f || hipWidthMapped <= 0f) {
            return GarmentTransformResult.InvalidAnchor
        }

        // Handle aspect ratio
        val garmentWidth = asset.texture.width.toFloat()
        val garmentHeight = asset.texture.height.toFloat()
        if (garmentWidth <= 0f || garmentHeight <= 0f) {
            return GarmentTransformResult.InvalidAnchor
        }
        val garmentAspectRatio = garmentWidth / garmentHeight

        // Rotate using the shoulders' screen vector (handles mirroring and rotation natively)
        // Ensure vector goes from screen-left to screen-right for consistent angle signs
        val screenLeftShoulder = if (leftShoulderMapped.x < rightShoulderMapped.x) leftShoulderMapped else rightShoulderMapped
        val screenRightShoulder = if (leftShoulderMapped.x < rightShoulderMapped.x) rightShoulderMapped else leftShoulderMapped
        val dxVec = screenRightShoulder.x - screenLeftShoulder.x
        val dyVec = screenRightShoulder.y - screenLeftShoulder.y
        val rotation = Math.toDegrees(kotlin.math.atan2(dyVec.toDouble(), dxVec.toDouble())).toFloat()

        // 4. Calculate dimensions and positioning based on category-aware strategy
        val width: Float
        val height: Float
        val translationX: Float
        val translationY: Float

        when (placementType) {
            PlacementType.UPPER -> {
                val multiplier = when {
                    asset.category.lowercase().contains("jacket") || asset.category.lowercase().contains("coat") -> 1.25f
                    else -> 1.15f
                }
                width = shoulderWidthMapped * multiplier
                height = width / garmentAspectRatio
                
                // Center on the torso (average of shoulders and hips)
                translationX = (leftShoulderMapped.x + rightShoulderMapped.x + leftHipMapped.x + rightHipMapped.x) / 4f
                
                // Shoulder line midpoint
                val shoulderLineY = (leftShoulderMapped.y + rightShoulderMapped.y) / 2f
                // Center Y sits at shoulderLineY + 40% of garment height, positioning collar correctly
                translationY = shoulderLineY + height * 0.40f
            }
            PlacementType.LOWER -> {
                width = hipWidthMapped * 1.20f
                height = width / garmentAspectRatio

                // Center on the waist
                translationX = (leftHipMapped.x + rightHipMapped.x) / 2f
                
                val hipLineY = (leftHipMapped.y + rightHipMapped.y) / 2f
                // Center Y sits at hipLineY + 45% of garment height, positioning waistband correctly
                translationY = hipLineY + height * 0.45f
            }
            PlacementType.DRESS -> {
                width = shoulderWidthMapped * 1.15f
                height = width / garmentAspectRatio

                // Center on the torso
                translationX = (leftShoulderMapped.x + rightShoulderMapped.x + leftHipMapped.x + rightHipMapped.x) / 4f
                
                val shoulderLineY = (leftShoulderMapped.y + rightShoulderMapped.y) / 2f
                translationY = shoulderLineY + height * 0.42f
            }
        }

        return GarmentTransformResult.Success(
            translationX = translationX,
            translationY = translationY,
            width = width,
            height = height,
            rotation = rotation
        )
    }
}
