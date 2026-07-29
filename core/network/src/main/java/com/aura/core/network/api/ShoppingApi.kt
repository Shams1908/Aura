package com.aura.core.network.api

import com.aura.core.network.model.ProductDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ShoppingApi {
    @Multipart
    @POST("v1/shopping/visual-search")
    suspend fun findSimilarProductsByImage(
        @Part image: MultipartBody.Part,
        @Query("stores") stores: List<String>? = null
    ): List<ProductDto>

    @GET("v1/shopping/products")
    suspend fun queryProducts(
        @Query("query") query: String,
        @Query("stores") stores: List<String>? = null
    ): List<ProductDto>
}
