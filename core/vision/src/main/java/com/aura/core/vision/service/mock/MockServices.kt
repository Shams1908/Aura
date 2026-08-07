package com.aura.core.vision.service.mock

import android.graphics.RectF
import com.aura.core.vision.model.*
import com.aura.core.vision.service.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTrackingEngine @Inject constructor() : TrackingEngine {
    override suspend fun track(frame: VisionFrame): TrackingResult {
        return TrackingResult(
            isTracking = true,
            boundingBoxes = listOf(RectF(100f, 150f, 300f, 450f))
        )
    }
}

@Singleton
class MockPoseEstimator @Inject constructor() : PoseEstimator {
    override suspend fun estimatePose(frame: VisionFrame): PoseResult {
        return PoseResult(
            landmarks = listOf(
                PoseResult.PoseLandmark(0, 0.5f, 0.2f, 0.0f, 0.99f), // nose
                PoseResult.PoseLandmark(11, 0.4f, 0.4f, 0.1f, 0.95f), // left shoulder
                PoseResult.PoseLandmark(12, 0.6f, 0.4f, 0.1f, 0.95f)  // right shoulder
            )
        )
    }
}

@Singleton
class MockGarmentSegmenter @Inject constructor() : GarmentSegmenter {
    override suspend fun segmentGarments(frame: VisionFrame): SegmentationResult {
        return SegmentationResult(
            maskWidth = 64,
            maskHeight = 64,
            labelIndices = IntArray(64 * 64) { if (it in 1000..3000) 1 else 0 }
        )
    }
}

@Singleton
class MockStyleAnalyzer @Inject constructor() : StyleAnalyzer {
    override suspend fun analyzeStyle(frame: VisionFrame): StyleResult {
        return StyleResult(
            primaryColor = "Navy Blue",
            secondaryColor = "Soft Ivory",
            pattern = "Solid Minimalist",
            confidence = 0.92f
        )
    }
}

@Singleton
class MockVirtualTryOnEngine @Inject constructor() : VirtualTryOnEngine {
    override suspend fun processTryOn(frame: VisionFrame): TryOnResult {
        return TryOnResult(
            outputBitmap = frame.bitmap,
            isSuccessful = true
        )
    }
}

@Singleton
class MockRecommendationEngine @Inject constructor() : RecommendationEngine {
    override suspend fun getRecommendations(frame: VisionFrame): RecommendationResult {
        return RecommendationResult(
            recommendedItemIds = listOf("outfit_chic_blazer_01", "outfit_classic_denim_02"),
            alignmentScores = mapOf("outfit_chic_blazer_01" to 0.95f, "outfit_classic_denim_02" to 0.88f)
        )
    }
}
