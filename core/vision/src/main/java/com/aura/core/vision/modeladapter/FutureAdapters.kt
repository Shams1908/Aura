package com.aura.core.vision.modeladapter

import com.aura.core.vision.service.*

/**
 * Interface representing a future MediaPipe implementation of the pose estimation and tracking.
 */
interface MediaPipePoseEstimator : PoseEstimator

/**
 * Interface representing a future ViTPose implementation of pose estimation.
 */
interface ViTPoseEstimator : PoseEstimator

/**
 * Interface representing a future SAM2 (Segment Anything 2) implementation of garment segmentation.
 */
interface SAM2Segmenter : GarmentSegmenter

/**
 * Interface representing a future GroundingDINO implementation of object detection and tracking.
 */
interface GroundingDinoTracker : TrackingEngine

/**
 * Interface representing a future IDM-VTON implementation of virtual try-on.
 */
interface IdmVtonEngine : VirtualTryOnEngine

/**
 * Interface representing a future CatVTON implementation of virtual try-on.
 */
interface CatVtonEngine : VirtualTryOnEngine

/**
 * Interface representing a future Florence implementation of fine-grained style analysis and description.
 */
interface FlorenceAnalyzer : StyleAnalyzer

/**
 * Interface representing a future Gemini implementation of style recommendation and multimodal query support.
 */
interface GeminiAnalyzer : StyleAnalyzer

/**
 * Interface representing a future OpenAI implementation of style analysis and conversational recommendation.
 */
interface OpenAIAnalyzer : StyleAnalyzer
