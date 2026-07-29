package com.aura.feature.shopping.domain

import android.graphics.Bitmap
import com.aura.core.network.model.ProductDto
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    suspend fun searchSimilarProducts(
        image: Bitmap,
        allowedStores: List<String>
    ): Flow<com.aura.core.common.result.Result<List<ProductDto>>>
    
    suspend fun querySimilarProductsByText(
        queryText: String,
        allowedStores: List<String>
    ): Flow<com.aura.core.common.result.Result<List<ProductDto>>>
}
