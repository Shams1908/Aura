package com.aura.core.common.session

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: InMemorySessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSessionManager(
        impl: SessionManagerImpl
    ): SessionManager
}
