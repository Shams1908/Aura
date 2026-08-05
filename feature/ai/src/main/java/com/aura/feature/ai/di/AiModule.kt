package com.aura.feature.ai.di

import com.aura.feature.ai.domain.GarmentParser
import com.aura.feature.ai.domain.MockGarmentParserImpl
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
}
