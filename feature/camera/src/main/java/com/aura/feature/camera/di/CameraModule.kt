package com.aura.feature.camera.di

import com.aura.core.vision.service.PoseEstimator
import com.aura.feature.camera.domain.PoseDetectorEngine
import com.aura.feature.camera.domain.PoseDetectorEngineImpl
import com.aura.feature.camera.domain.PoseEstimatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {

    @Binds
    @Singleton
    abstract fun bindPoseDetectorEngine(
        poseDetectorEngineImpl: PoseDetectorEngineImpl
    ): PoseDetectorEngine

    @Binds
    @Singleton
    abstract fun bindPoseEstimator(
        poseEstimatorImpl: PoseEstimatorImpl
    ): PoseEstimator
}
