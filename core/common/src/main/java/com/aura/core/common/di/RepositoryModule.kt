package com.aura.core.common.di

import com.aura.core.common.data.RemoteOutfitRepositoryImpl
import com.aura.core.common.data.OutfitRepository
import com.aura.core.common.network.LiveNetworkMonitor
import com.aura.core.common.network.NetworkMonitor
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
        impl: RemoteOutfitRepositoryImpl
    ): OutfitRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: LiveNetworkMonitor
    ): NetworkMonitor
}
