package com.aura.core.common.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aura.core.network.api.AuraBackendApi

class OutfitPagingSource(
    private val auraBackendApi: AuraBackendApi,
    private val query: String
) : PagingSource<Int, OutfitModel>() {

    override fun getRefreshKey(state: PagingState<Int, OutfitModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OutfitModel> {
        val page = params.key ?: 1
        return try {
            val response = auraBackendApi.searchOutfits(
                query = query,
                page = page,
                limit = params.loadSize
            )
            
            val outfitsList = response.data.map { it.toDomain() }
            
            LoadResult.Page(
                data = outfitsList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (outfitsList.isEmpty() || (page * params.loadSize) >= response.total) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
