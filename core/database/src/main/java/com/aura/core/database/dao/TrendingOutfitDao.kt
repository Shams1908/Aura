package com.aura.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.database.entity.TrendingOutfitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendingOutfitDao {
    @Query("SELECT * FROM trending_outfits ORDER BY cachedAt DESC")
    fun getCachedTrendingOutfits(): Flow<List<TrendingOutfitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrendingOutfits(outfits: List<TrendingOutfitEntity>)

    @Query("DELETE FROM trending_outfits")
    suspend fun clearTrendingOutfits()
}
