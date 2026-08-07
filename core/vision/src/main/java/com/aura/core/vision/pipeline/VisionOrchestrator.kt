package com.aura.core.vision.pipeline

import com.aura.core.vision.model.*
import com.aura.core.vision.service.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisionOrchestrator @Inject constructor(
    private val trackingEngine: TrackingEngine,
    private val poseEstimator: PoseEstimator,
    private val garmentSegmenter: GarmentSegmenter,
    private val styleAnalyzer: StyleAnalyzer,
    private val tryOnEngine: VirtualTryOnEngine,
    private val recommendationEngine: RecommendationEngine
) : FrameProcessor {

    override suspend fun process(frame: VisionFrame): PipelineResult {
        // 1. Frame -> Tracking
        val trackingResult = trackingEngine.track(frame)
        
        // 2. Tracking -> Pose
        val poseResult = poseEstimator.estimatePose(frame)
        
        // 3. Pose -> Segmentation
        val segmentationResult = garmentSegmenter.segmentGarments(frame)
        
        // 4. Segmentation -> Style
        val styleResult = styleAnalyzer.analyzeStyle(frame)
        
        // 5. Style -> Try-On
        val tryOnResult = tryOnEngine.processTryOn(frame)
        
        // 6. Try-On -> Recommendations
        val recommendationResult = recommendationEngine.getRecommendations(frame)
        
        // 7. Recommendations -> Overlay
        val overlayInstructions = generateOverlayInstructions(trackingResult, poseResult, segmentationResult, styleResult)

        return PipelineResult(
            tracking = trackingResult,
            pose = poseResult,
            segmentation = segmentationResult,
            style = styleResult,
            tryOn = tryOnResult,
            recommendations = recommendationResult,
            overlay = overlayInstructions,
            timestampMs = frame.metadata.timestampMs
        )
    }

    private fun generateOverlayInstructions(
        tracking: TrackingResult,
        pose: PoseResult,
        segmentation: SegmentationResult,
        style: StyleResult
    ): OverlayInstructions {
        val instructions = mutableListOf<String>()
        if (tracking.isTracking) {
            instructions.add("Draw bounding box at body bounds")
        }
        if (pose.landmarks.isNotEmpty()) {
            instructions.add("Draw 2D skeleton joints for posture correction")
        }
        if (segmentation.labelIndices.any { it > 0 }) {
            instructions.add("Highlight detected garments")
        }
        instructions.add("Detected Style: ${style.primaryColor} - ${style.pattern}")
        return OverlayInstructions(instructions)
    }
}
