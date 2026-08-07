package com.aura.core.vision.di

import com.aura.core.vision.provider.DefaultFrameProvider
import com.aura.core.vision.provider.FrameProvider
import com.aura.core.vision.pipeline.VisionPipeline
import com.aura.core.vision.pipeline.VisionPipelineImpl
import com.aura.core.vision.service.*
import com.aura.core.vision.service.mock.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VisionModule {

    @Binds
    abstract fun bindFrameProvider(
        defaultFrameProvider: DefaultFrameProvider
    ): FrameProvider

    @Binds
    abstract fun bindVisionPipeline(
        visionPipelineImpl: VisionPipelineImpl
    ): VisionPipeline

    @Binds
    abstract fun bindTrackingEngine(
        mockTrackingEngine: MockTrackingEngine
    ): TrackingEngine

    @Binds
    abstract fun bindPoseEstimator(
        mockPoseEstimator: MockPoseEstimator
    ): PoseEstimator

    @Binds
    abstract fun bindGarmentSegmenter(
        mockGarmentSegmenter: MockGarmentSegmenter
    ): GarmentSegmenter

    @Binds
    abstract fun bindStyleAnalyzer(
        mockStyleAnalyzer: MockStyleAnalyzer
    ): StyleAnalyzer

    @Binds
    abstract fun bindVirtualTryOnEngine(
        mockVirtualTryOnEngine: MockVirtualTryOnEngine
    ): VirtualTryOnEngine

    @Binds
    abstract fun bindRecommendationEngine(
        mockRecommendationEngine: MockRecommendationEngine
    ): RecommendationEngine
}
