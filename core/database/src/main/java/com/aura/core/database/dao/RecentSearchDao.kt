package com.aura.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.core.database.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE queryText = :queryText")
    suspend fun deleteRecentSearch(queryText: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()
}
