package com.aura.core.common.session

import com.aura.core.common.data.OutfitModel
import kotlinx.serialization.Serializable

/**
 * Type-safe identifier for an Outfit Session.
 */
@Serializable
@JvmInline
value class OutfitSessionId(val value: String)

/**
 * Immutable lifecycle stages for an Aura Outfit Session.
 */
enum class SessionLifecycleStage {
    CREATED,
    OUTFIT_ATTACHED,
    STUDIO_OPENED,
    CAMERA_READY,
    TRACKING_READY,
    ANALYSIS_READY,
    TRY_ON_READY,
    SHOPPING_READY
}

/**
 * AI Pose Estimation placeholder result.
 */
@Serializable
data class PoseResultPlaceholder(val landmarks: List<String>)

/**
 * AI Segmentation placeholder result.
 */
@Serializable
data class SegmentationResultPlaceholder(val segmentMaskUrl: String)

/**
 * Virtual Try-On mapping placeholder result.
 */
@Serializable
data class TryOnResultPlaceholder(val generatedOutfitImageUrl: String)

/**
 * AI Style Analysis scoring placeholder result.
 */
@Serializable
data class StyleAnalysisResultPlaceholder(val styleScore: Float, val feedbackTags: List<String>)

/**
 * AI Recommendation list placeholder result.
 */
@Serializable
data class RecommendationResultPlaceholder(val suggestedProductIds: List<String>)

/**
 * Model representing a unified Aura Outfit Session, serving as the single source of truth.
 */
@Serializable
data class OutfitSession(
    val sessionId: OutfitSessionId,
    val stage: SessionLifecycleStage = SessionLifecycleStage.CREATED,
    val referenceOutfitUri: String? = null,
    val referenceOutfitMetadata: OutfitModel? = null,
    
    // Workflow status descriptions
    val cameraState: String = "IDLE",
    val trackingState: String = "WAITING",
    val analysisStatus: String = "PENDING",
    val tryOnStatus: String = "NOT_STARTED",
    val shoppingStatus: String = "NOT_READY",
    
    val createdTime: Long = System.currentTimeMillis(),
    val updatedTime: Long = System.currentTimeMillis(),
    
    // Future AI result placeholders (nullable)
    val poseResult: PoseResultPlaceholder? = null,
    val segmentationResult: SegmentationResultPlaceholder? = null,
    val virtualTryOnResult: TryOnResultPlaceholder? = null,
    val styleAnalysisResult: StyleAnalysisResultPlaceholder? = null,
    val recommendationResult: RecommendationResultPlaceholder? = null
)
