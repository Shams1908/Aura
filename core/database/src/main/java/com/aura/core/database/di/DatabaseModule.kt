package com.aura.core.database.di

import android.content.Context
import androidx.room.Room
import com.aura.core.database.AppDatabase
import com.aura.core.database.dao.SavedOutfitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aura_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun provideSavedOutfitDao(
        database: AppDatabase
    ): SavedOutfitDao {
        return database.savedOutfitDao()
    }
}
