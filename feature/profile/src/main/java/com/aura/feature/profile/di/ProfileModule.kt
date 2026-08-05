package com.aura.feature.profile.di

import com.aura.feature.profile.data.FakeUserPhotoRepository
import com.aura.feature.profile.domain.repository.UserPhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI configuration module for Profile feature.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindUserPhotoRepository(
        fakeRepositoryImpl: FakeUserPhotoRepository
    ): UserPhotoRepository
}
