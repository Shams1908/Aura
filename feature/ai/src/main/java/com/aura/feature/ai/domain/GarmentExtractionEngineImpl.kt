package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.core.common.data.ReferenceImage
import com.aura.feature.ai.detector.YoloDetector
import com.aura.feature.ai.inference.AIError
import com.aura.feature.ai.model.GarmentAsset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GarmentExtractionEngineImpl @Inject constructor(
    private val yoloDetector: YoloDetector
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

            // 3. Evaluate segmentation blocker
            // Since fashion_detector.onnx is an object detection model and there is no bundled garment
            // segmentation model in the assets, we stop here and return MODEL_UNAVAILABLE as requested.
            println("AURA_DEBUG: GarmentExtractionEngineImpl: Real pixel-level garment segmentation model is currently unavailable.")
            return Result.failure(AIError.ModelUnavailable)

        } catch (e: Exception) {
            println("AURA_DEBUG: GarmentExtractionEngineImpl: Extraction failed: ${e.message}")
            if (e is AIError) return Result.failure(e)
            return Result.failure(AIError.ProcessingFailed)
        }
    }
}
