package com.aura.core.common.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.aura.core.database.dao.RecentSearchDao
import com.aura.core.database.dao.TrendingOutfitDao
import com.aura.core.database.dao.ViewedOutfitDao
import com.aura.core.database.entity.RecentSearchEntity
import com.aura.core.database.entity.TrendingOutfitEntity
import com.aura.core.database.entity.ViewedOutfitEntity
import com.aura.core.common.network.NetworkMonitor
import com.aura.core.network.api.AuraBackendApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteOutfitRepositoryImpl @Inject constructor(
    private val auraBackendApi: AuraBackendApi,
    private val trendingOutfitDao: TrendingOutfitDao,
    private val recentSearchDao: RecentSearchDao,
    private val viewedOutfitDao: ViewedOutfitDao,
    private val networkMonitor: NetworkMonitor
) : OutfitRepository {

    override fun getTrendingOutfits(forceRefresh: Boolean): Flow<List<OutfitModel>> = flow {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            try {
                val remoteTrending = auraBackendApi.getTrendingOutfits()
                val dbEntities = remoteTrending.map { dto ->
                    TrendingOutfitEntity(
                        id = dto.id,
                        title = dto.title,
                        brand = dto.brand,
                        description = dto.description,
                        imageUrl = dto.imageUrl,
                        category = dto.category,
                        color = dto.color,
                        tags = dto.tags.joinToString(","),
                        price = dto.price,
                        cachedAt = System.currentTimeMillis()
                    )
                }
                trendingOutfitDao.clearTrendingOutfits()
                trendingOutfitDao.insertTrendingOutfits(dbEntities)
            } catch (e: Exception) {
                // Fallback to cache silently on network errors
            }
        }
        
        // Always emit cache as source of truth
        trendingOutfitDao.getCachedTrendingOutfits().collect { cached ->
            emit(cached.map { entity ->
                OutfitModel(
                    id = entity.id,
                    title = entity.title,
                    brand = entity.brand,
                    description = entity.description,
                    imageUrl = entity.imageUrl,
                    category = entity.category,
                    color = entity.color,
                    tags = entity.tags.split(",").filter { it.isNotBlank() },
                    price = entity.price
                )
            })
        }
    }

    override fun getRecommendedOutfits(): Flow<List<OutfitModel>> = flow {
        // Recommendations are a dynamic feed from backend
        try {
            val remote = auraBackendApi.getTrendingOutfits()
            emit(remote.shuffled().map { it.toDomain() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun searchOutfits(query: String): Flow<List<OutfitModel>> = flow {
        try {
            val searchResponse = auraBackendApi.searchOutfits(query, page = 1, limit = 20)
            emit(searchResponse.data.map { it.toDomain() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun searchOutfitsPaged(query: String): Flow<PagingData<OutfitModel>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { OutfitPagingSource(auraBackendApi, query) }
        ).flow
    }

    override fun getOutfitDetails(id: String): Flow<OutfitModel?> = flow {
        try {
            val details = auraBackendApi.getOutfitDetails(id)
            val mapped = details.toDomain()
            addOutfitToViewedHistory(mapped)
            emit(mapped)
        } catch (e: Exception) {
            emit(null)
        }
    }

    override fun getSimilarOutfits(id: String): Flow<List<OutfitModel>> = flow {
        try {
            val similar = auraBackendApi.getSimilarOutfits(id)
            emit(similar.map { it.toDomain() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getRecentlyViewedOutfits(): Flow<List<OutfitModel>> {
        return viewedOutfitDao.getRecentlyViewedOutfits().map { viewedList ->
            viewedList.map { entity ->
                OutfitModel(
                    id = entity.id,
                    title = entity.title,
                    brand = entity.brand,
                    description = "",
                    imageUrl = entity.imageUrl,
                    category = "",
                    color = "",
                    tags = emptyList(),
                    price = 0.0
                )
            }
        }
    }

    override suspend fun addOutfitToViewedHistory(outfit: OutfitModel) {
        viewedOutfitDao.insertViewedOutfit(
            ViewedOutfitEntity(
                id = outfit.id,
                title = outfit.title,
                brand = outfit.brand,
                imageUrl = outfit.imageUrl,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return recentSearchDao.getRecentSearches().map { searches ->
            searches.map { it.queryText }
        }
    }

    override suspend fun addSearchQueryToHistory(query: String) {
        if (query.isNotBlank()) {
            recentSearchDao.insertRecentSearch(
                RecentSearchEntity(
                    queryText = query.trim(),
                    searchedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
