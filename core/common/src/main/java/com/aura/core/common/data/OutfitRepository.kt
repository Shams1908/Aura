package com.aura.core.common.data

import kotlinx.coroutines.flow.Flow

interface OutfitRepository {
    fun getTrendingOutfits(): Flow<List<OutfitModel>>
    fun getRecommendedOutfits(): Flow<List<OutfitModel>>
    fun searchOutfits(query: String): Flow<List<OutfitModel>>
    fun getOutfitDetails(id: String): Flow<OutfitModel?>
}
