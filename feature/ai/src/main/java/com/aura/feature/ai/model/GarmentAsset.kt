package com.aura.feature.ai.model

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Reusable internal representation of an extracted garment, independent of live frames.
 */
data class GarmentAsset(
    val category: String,
    val texture: Bitmap,
    val alphaMask: Bitmap,
    val originalDimensions: Pair<Int, Int>,
    val normalizedBounds: RectF,
    val confidence: Float,
    val sourceReferenceUri: String
)
