package com.aura.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.core.database.dao.RecentSearchDao
import com.aura.core.database.dao.SavedOutfitDao
import com.aura.core.database.dao.TrendingOutfitDao
import com.aura.core.database.dao.ViewedOutfitDao
import com.aura.core.database.entity.RecentSearchEntity
import com.aura.core.database.entity.SavedOutfitEntity
import com.aura.core.database.entity.TrendingOutfitEntity
import com.aura.core.database.entity.ViewedOutfitEntity

@Database(
    entities = [
        SavedOutfitEntity::class,
        TrendingOutfitEntity::class,
        RecentSearchEntity::class,
        ViewedOutfitEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedOutfitDao(): SavedOutfitDao
    abstract fun trendingOutfitDao(): TrendingOutfitDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun viewedOutfitDao(): ViewedOutfitDao
}
