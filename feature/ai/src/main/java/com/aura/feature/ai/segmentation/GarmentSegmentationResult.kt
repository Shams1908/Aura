package com.aura.feature.ai.segmentation

import android.graphics.Bitmap

enum class SegmentationStatus {
    SUCCESS,
    FAILED,
    NO_CLOTHES_FOUND
}

/**
 * Model-independent result holding the segmented garment mask and metadata.
 */
data class GarmentSegmentationResult(
    val mask: Bitmap,
    val width: Int,
    val height: Int,
    val confidence: Float,
    val status: SegmentationStatus
)
