package com.aura.feature.ai.repository

import android.graphics.Bitmap
import com.aura.feature.ai.model.ClothingItem
import com.aura.feature.ai.model.OutfitAnalysis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FashionVisionRepositoryImpl @Inject constructor() : FashionVisionRepository {
    override suspend fun analyze(bitmap: Bitmap): OutfitAnalysis {
        return OutfitAnalysis(
            items = listOf(
                ClothingItem(category = "Blazer", color = "Navy Blue", material = "Wool", confidence = 0.95f),
                ClothingItem(category = "T-Shirt", color = "Soft Ivory", material = "Cotton", confidence = 0.92f)
            ),
            style = "Casual Chic",
            caption = "A stylish look featuring a navy blue blazer over a soft ivory t-shirt."
        )
    }
}
