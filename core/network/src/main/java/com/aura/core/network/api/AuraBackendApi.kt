package com.aura.core.network.api

import com.aura.core.network.model.OutfitDto
import com.aura.core.network.model.OutfitListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AuraBackendApi {

    @GET("api/v1/outfits/search")
    suspend fun searchOutfits(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): OutfitListResponse

    @GET("api/v1/outfits/trending")
    suspend fun getTrendingOutfits(): List<OutfitDto>

    @GET("api/v1/outfits/categories")
    suspend fun getCategories(): List<String>

    @GET("api/v1/outfits/{id}")
    suspend fun getOutfitDetails(
        @Path("id") id: String
    ): OutfitDto

    @GET("api/v1/outfits/{id}/similar")
    suspend fun getSimilarOutfits(
        @Path("id") id: String
    ): List<OutfitDto>
}
