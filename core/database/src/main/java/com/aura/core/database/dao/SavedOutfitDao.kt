package com.aura.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.database.entity.SavedOutfitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedOutfitDao {
    @Query("SELECT * FROM saved_outfits ORDER BY savedAt DESC")
    fun getAllSavedOutfits(): Flow<List<SavedOutfitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOutfit(outfit: SavedOutfitEntity)

    @Delete
    suspend fun deleteOutfit(outfit: SavedOutfitEntity)

    @Query("SELECT EXISTS(SELECT * FROM saved_outfits WHERE id = :id)")
    fun isOutfitSaved(id: String): Flow<Boolean>
}
