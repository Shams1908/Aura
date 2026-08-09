package com.aura.feature.ai.di

import com.aura.feature.ai.detector.YoloDetector
import com.aura.feature.ai.detector.YoloDetectorImpl
import com.aura.feature.ai.domain.GarmentParser
import com.aura.feature.ai.domain.MockGarmentParserImpl
import com.aura.feature.ai.inference.InferenceEngine
import com.aura.feature.ai.inference.DefaultInferenceEngine
import com.aura.feature.ai.repository.FashionVisionRepository
import com.aura.feature.ai.repository.FashionVisionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindGarmentParser(
        garmentParserImpl: MockGarmentParserImpl
    ): GarmentParser

    @Binds
    @Singleton
    abstract fun bindInferenceEngine(
        defaultInferenceEngine: DefaultInferenceEngine
    ): InferenceEngine

    @Binds
    @Singleton
    abstract fun bindFashionVisionRepository(
        fashionVisionRepositoryImpl: FashionVisionRepositoryImpl
    ): FashionVisionRepository

    @Binds
    @Singleton
    abstract fun bindYoloDetector(
        yoloDetectorImpl: YoloDetectorImpl
    ): YoloDetector
}
