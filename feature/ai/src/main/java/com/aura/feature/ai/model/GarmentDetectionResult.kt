package com.aura.feature.ai.model

import android.graphics.RectF
import android.graphics.Bitmap

/**
 * Structured output representing the real ML detection result of a garment.
 */
data class GarmentDetectionResult(
    val category: String,
    val confidence: Float,
    val boundingRegion: RectF,
    val segmentationMask: Bitmap?,
    val sourceImageDimensions: Pair<Int, Int>
)
