package com.aura.core.vision.service

import com.aura.core.vision.model.VisionFrame
import com.aura.core.vision.model.PoseResult
import com.aura.core.vision.model.SegmentationResult
import com.aura.core.vision.model.StyleResult
import com.aura.core.vision.model.TryOnResult
import com.aura.core.vision.model.TrackingResult
import com.aura.core.vision.model.RecommendationResult

interface TrackingEngine {
    suspend fun track(frame: VisionFrame): TrackingResult
}

interface PoseEstimator {
    suspend fun estimatePose(frame: VisionFrame): PoseResult
}

interface GarmentSegmenter {
    suspend fun segmentGarments(frame: VisionFrame): SegmentationResult
}

interface StyleAnalyzer {
    suspend fun analyzeStyle(frame: VisionFrame): StyleResult
}

interface VirtualTryOnEngine {
    suspend fun processTryOn(frame: VisionFrame): TryOnResult
}

interface RecommendationEngine {
    suspend fun getRecommendations(frame: VisionFrame): RecommendationResult
}
