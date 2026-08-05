package com.aura.feature.ai.data

import com.aura.feature.ai.domain.DetectedClothingItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeOutfitRepository @Inject constructor() {

    fun getTips(): List<String> {
        return listOf(
            "Upload clear, front-facing outfit images for the most accurate AI analysis.",
            "Ensure natural and bright lighting when taking photos of your garments.",
            "A plain background helps the AI segment individual garments much faster."
        )
    }

    fun detectGarments(imageUri: String): Flow<List<DetectedClothingItem>> = flow {
        delay(1200) // Simulate processing time
        emit(
            listOf(
                DetectedClothingItem(id = "1", name = "Oversized Hoodie", confidence = 0.94f, category = "Upper Body"),
                DetectedClothingItem(id = "2", name = "Straight Fit Jeans", confidence = 0.89f, category = "Lower Body"),
                DetectedClothingItem(id = "3", name = "White Sneakers", confidence = 0.95f, category = "Footwear"),
                DetectedClothingItem(id = "4", name = "Silver Watch", confidence = 0.82f, category = "Accessories")
            )
        )
    }
}
