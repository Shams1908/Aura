package com.aura.core.vision.model

import android.graphics.Bitmap
import android.graphics.RectF

data class TrackingResult(
    val isTracking: Boolean,
    val boundingBoxes: List<RectF>
)

data class PoseResult(
    val landmarks: List<PoseLandmark>,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val rotationDegrees: Int = 0,
    val isFrontCamera: Boolean = false
) {
    data class PoseLandmark(val id: Int, val x: Float, val y: Float, val z: Float, val likelihood: Float)
}

data class SegmentationResult(
    val maskWidth: Int,
    val maskHeight: Int,
    val labelIndices: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SegmentationResult) return false
        if (maskWidth != other.maskWidth) return false
        if (maskHeight != other.maskHeight) return false
        return labelIndices.contentEquals(other.labelIndices)
    }

    override fun hashCode(): Int {
        var result = maskWidth
        result = 31 * result + maskHeight
        result = 31 * result + labelIndices.contentHashCode()
        return result
    }
}

data class StyleResult(
    val primaryColor: String,
    val secondaryColor: String,
    val pattern: String,
    val confidence: Float
)

data class TryOnResult(
    val outputBitmap: Bitmap?,
    val isSuccessful: Boolean
)

data class RecommendationResult(
    val recommendedItemIds: List<String>,
    val alignmentScores: Map<String, Float>
)

data class OverlayInstructions(
    val instructions: List<String>
)

data class PipelineResult(
    val tracking: TrackingResult?,
    val pose: PoseResult?,
    val segmentation: SegmentationResult?,
    val style: StyleResult?,
    val tryOn: TryOnResult?,
    val recommendations: RecommendationResult?,
    val overlay: OverlayInstructions?,
    val timestampMs: Long
)
