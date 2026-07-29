package com.aura.core.common.di

import com.aura.core.common.data.MockOutfitRepositoryImpl
import com.aura.core.common.data.OutfitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOutfitRepository(
        impl: MockOutfitRepositoryImpl
    ): OutfitRepository
}
