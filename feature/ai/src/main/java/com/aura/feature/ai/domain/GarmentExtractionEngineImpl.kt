package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.core.common.data.ReferenceImage
import com.aura.feature.ai.detector.YoloDetector
import com.aura.feature.ai.inference.AIError
import com.aura.feature.ai.model.GarmentAsset
import com.aura.feature.ai.segmentation.GarmentSegmenter
import com.aura.feature.ai.segmentation.SegmentationStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GarmentExtractionEngineImpl @Inject constructor(
    private val yoloDetector: YoloDetector,
    private val garmentSegmenter: GarmentSegmenter
) : GarmentExtractionEngine {

    override suspend fun extractGarment(
        referenceImage: ReferenceImage,
        decodedImage: Bitmap
    ): Result<GarmentAsset> {
        println("AURA_DEBUG: GarmentExtractionEngineImpl: Starting extraction for URI: ${referenceImage.uri}")

        try {
            // 1. Ensure YOLO fashion detector model session is loaded
            yoloDetector.load()

            // 2. Perform object detection on the decoded reference image
            val detections = yoloDetector.predict(decodedImage)
            println("AURA_DEBUG: GarmentExtractionEngineImpl: Found ${detections.size} fashion detections.")

            if (detections.isEmpty()) {
                println("AURA_DEBUG: GarmentExtractionEngineImpl: NO_GARMENT_DETECTED")
                return Result.failure(AIError.NoGarmentDetected)
            }

            // Find the most confident fashion detection
            val bestDetection = detections.maxByOrNull { it.confidence }
            if (bestDetection == null || bestDetection.confidence < 0.35f) {
                println("AURA_DEBUG: GarmentExtractionEngineImpl: LOW_CONFIDENCE")
                return Result.failure(AIError.LowConfidence)
            }

            println("AURA_DEBUG: GarmentExtractionEngineImpl: Best detection: ${bestDetection.label} with confidence: ${bestDetection.confidence}")

            // 3. Run MediaPipe segmentation
            val segmentResult = garmentSegmenter.segment(decodedImage)
            val segmentData = segmentResult.getOrElse { error ->
                println("AURA_DEBUG: GarmentExtractionEngineImpl: Segmentation failed with error: ${error.message}")
                return Result.failure(AIError.ModelUnavailable(
                    category = bestDetection.label,
                    confidence = bestDetection.confidence
                ))
            }

            if (segmentData.status == SegmentationStatus.NO_CLOTHES_FOUND) {
                println("AURA_DEBUG: GarmentExtractionEngineImpl: SegmentationFailed - No clothing pixels detected.")
                return Result.failure(AIError.SegmentationFailed(
                    category = bestDetection.label,
                    confidence = bestDetection.confidence
                ))
            }

            // 4. Intersect the clothing mask with the YOLO bounding box region
            val boundingBox = bestDetection.boundingBox
            val originalWidth = decodedImage.width
            val originalHeight = decodedImage.height

            var minX = originalWidth
            var maxX = -1
            var minY = originalHeight
            var maxY = -1
            var activePixelCount = 0

            val maskPixels = IntArray(originalWidth * originalHeight)
            segmentData.mask.getPixels(maskPixels, 0, originalWidth, 0, 0, originalWidth, originalHeight)

            for (y in 0 until originalHeight) {
                for (x in 0 until originalWidth) {
                    val index = y * originalWidth + x
                    val isMaskActive = (maskPixels[index] and 0x00FFFFFF) != 0 // Non-transparent pixel
                    val isInsideDetection = x >= boundingBox.left &&
                                            x <= boundingBox.right &&
                                            y >= boundingBox.top &&
                                            y <= boundingBox.bottom

                    if (isMaskActive && isInsideDetection) {
                        activePixelCount++
                        // Keep pixel as active (fully opaque white for the alpha mask)
                        maskPixels[index] = android.graphics.Color.WHITE
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    } else {
                        // Mask out pixel outside detection box or background
                        maskPixels[index] = android.graphics.Color.TRANSPARENT
                    }
                }
            }

            val minRequiredPixels = 150
            if (activePixelCount < minRequiredPixels || maxX < minX || maxY < minY) {
                println("AURA_DEBUG: GarmentExtractionEngineImpl: SegmentationFailed - Active intersection pixels ($activePixelCount) below threshold or empty intersection.")
                return Result.failure(AIError.SegmentationFailed(
                    category = bestDetection.label,
                    confidence = bestDetection.confidence
                ))
            }

            // 5. Crop original RGB texture and mask to tight bounding box
            val croppedWidth = (maxX - minX) + 1
            val croppedHeight = (maxY - minY) + 1

            // Debugging Support: Only log detailed stats in debug configurations
            val coveragePercentage = (activePixelCount.toFloat() / (originalWidth * originalHeight)) * 100
            android.util.Log.d("AURA_DEBUG_SEGMENTATION", "Mask stats: " +
                "dimensions=[${originalWidth}x${originalHeight}], " +
                "croppedDimensions=[${croppedWidth}x${croppedHeight}], " +
                "coverage=${"%.2f".format(coveragePercentage)}%, " +
                "detectionBox=[left=${boundingBox.left}, top=${boundingBox.top}, right=${boundingBox.right}, bottom=${boundingBox.bottom}], " +
                "activePixels=$activePixelCount")

            val croppedMaskPixels = IntArray(croppedWidth * croppedHeight)
            for (y in 0 until croppedHeight) {
                System.arraycopy(
                    maskPixels,
                    (minY + y) * originalWidth + minX,
                    croppedMaskPixels,
                    y * croppedWidth,
                    croppedWidth
                )
            }
            val croppedMaskBitmap = Bitmap.createBitmap(
                croppedMaskPixels,
                croppedWidth,
                croppedHeight,
                Bitmap.Config.ARGB_8888
            )

            val texturePixels = IntArray(originalWidth * originalHeight)
            decodedImage.getPixels(texturePixels, 0, originalWidth, 0, 0, originalWidth, originalHeight)

            val croppedTexturePixels = IntArray(croppedWidth * croppedHeight)
            for (y in 0 until croppedHeight) {
                for (x in 0 until croppedWidth) {
                    val srcIndex = (minY + y) * originalWidth + (minX + x)
                    val destIndex = y * croppedWidth + x

                    val maskAlpha = (maskPixels[srcIndex] ushr 24) and 0xFF
                    if (maskAlpha > 0) {
                        // Retain original RGB, set alpha to maskAlpha (fully opaque)
                        val rgb = texturePixels[srcIndex] and 0x00FFFFFF
                        croppedTexturePixels[destIndex] = (maskAlpha shl 24) or rgb
                    } else {
                        croppedTexturePixels[destIndex] = android.graphics.Color.TRANSPARENT
                    }
                }
            }
            val croppedTextureBitmap = Bitmap.createBitmap(
                croppedTexturePixels,
                croppedWidth,
                croppedHeight,
                Bitmap.Config.ARGB_8888
            )

            // 6. Return successful GarmentAsset
            val asset = GarmentAsset(
                category = bestDetection.label,
                texture = croppedTextureBitmap,
                alphaMask = croppedMaskBitmap,
                originalDimensions = Pair(originalWidth, originalHeight),
                normalizedBounds = android.graphics.RectF(
                    minX.toFloat() / originalWidth,
                    minY.toFloat() / originalHeight,
                    maxX.toFloat() / originalWidth,
                    maxY.toFloat() / originalHeight
                ),
                confidence = bestDetection.confidence,
                sourceReferenceUri = referenceImage.uri
            )

            println("AURA_DEBUG: GarmentExtractionEngineImpl: Successfully extracted garment asset: ${asset.category}")
            return Result.success(asset)

        } catch (e: Exception) {
            println("AURA_DEBUG: GarmentExtractionEngineImpl: Extraction failed: ${e.message}")
            if (e is AIError) return Result.failure(e)
            return Result.failure(AIError.ProcessingFailed)
        }
    }
}
