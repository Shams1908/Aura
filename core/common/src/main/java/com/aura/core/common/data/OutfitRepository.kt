package com.aura.core.common.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface OutfitRepository {
    /**
     * Retrieves trending outfits. Resolves local cache first, then fetches fresh listings
     * from the network proxy when forceRefresh is set.
     */
    fun getTrendingOutfits(forceRefresh: Boolean = false): Flow<List<OutfitModel>>

    fun getRecommendedOutfits(): Flow<List<OutfitModel>>

    /**
     * Executes traditional text search on mock or static databases.
     */
    fun searchOutfits(query: String): Flow<List<OutfitModel>>

    /**
     * Provides a paginated Flow stream utilizing Paging 3 to display infinite fashion results.
     */
    fun searchOutfitsPaged(query: String): Flow<PagingData<OutfitModel>>

    fun getOutfitDetails(id: String): Flow<OutfitModel?>

    fun getSimilarOutfits(id: String): Flow<List<OutfitModel>>

    fun getRecentlyViewedOutfits(): Flow<List<OutfitModel>>

    suspend fun addOutfitToViewedHistory(outfit: OutfitModel)

    fun getRecentSearches(): Flow<List<String>>

    suspend fun addSearchQueryToHistory(query: String)
}
