package com.aura.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.database.entity.ViewedOutfitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ViewedOutfitDao {
    @Query("SELECT * FROM viewed_outfits ORDER BY viewedAt DESC LIMIT 20")
    fun getRecentlyViewedOutfits(): Flow<List<ViewedOutfitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViewedOutfit(outfit: ViewedOutfitEntity)

    @Query("DELETE FROM viewed_outfits")
    suspend fun clearViewedHistory()
}
