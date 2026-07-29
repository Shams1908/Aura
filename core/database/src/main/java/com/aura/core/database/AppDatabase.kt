package com.aura.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.core.database.dao.SavedOutfitDao
import com.aura.core.database.entity.SavedOutfitEntity

@Database(entities = [SavedOutfitEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedOutfitDao(): SavedOutfitDao
}
