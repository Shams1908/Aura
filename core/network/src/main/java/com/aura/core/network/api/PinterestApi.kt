package com.aura.core.network.api

import com.aura.core.network.model.PinSearchResponse
import com.aura.core.network.model.PinterestPinDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PinterestApi {
    @GET("v1/pins/search")
    suspend fun searchPins(
        @Query("query") query: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20
    ): PinSearchResponse

    @GET("v1/pins/trending")
    suspend fun getTrendingPins(
        @Query("limit") limit: Int = 20
    ): List<PinterestPinDto>

    @GET("v1/pins/{id}")
    suspend fun getPinDetail(
        @Path("id") pinId: String
    ): PinterestPinDto
}
