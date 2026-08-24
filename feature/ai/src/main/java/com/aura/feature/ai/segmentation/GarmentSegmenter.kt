package com.aura.feature.ai.segmentation

import android.graphics.Bitmap

/**
 * Model-independent interface contract for pixel-level garment segmentation.
 */
interface GarmentSegmenter {

    /**
     * Segments clothing regions from the input Bitmap.
     */
    suspend fun segment(bitmap: Bitmap): Result<GarmentSegmentationResult>
}
