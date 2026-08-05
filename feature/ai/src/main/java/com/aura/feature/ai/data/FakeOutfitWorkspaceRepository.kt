package com.aura.feature.ai.data

import com.aura.feature.ai.domain.model.DetectedClothing
import com.aura.feature.ai.domain.model.DetectedStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to simulate clothing detection and style analysis operations.
 */
@Singleton
class FakeOutfitWorkspaceRepository @Inject constructor() {

    /**
     * Retrieves a list of helper/onboarding tips.
     */
    fun getTips(): List<String> {
        return listOf(
            "Use front-facing outfit photos.",
            "Avoid blurry images.",
            "Higher resolution improves analysis."
        )
    }

    /**
     * Simulates scanning an image for garments.
     */
    fun detectClothing(imageUri: String): Flow<Result<List<DetectedClothing>>> = flow {
        delay(1200)
        if (imageUri.isBlank()) {
            emit(Result.failure(Exception("Image URI cannot be empty")))
        } else {
            emit(
                Result.success(
                    listOf(
                        DetectedClothing(id = "1", name = "Oversized Hoodie", confidence = 0.94f),
                        DetectedClothing(id = "2", name = "Cargo Pants", confidence = 0.88f),
                        DetectedClothing(id = "3", name = "White Sneakers", confidence = 0.95f),
                        DetectedClothing(id = "4", name = "Crossbody Bag", confidence = 0.81f)
                    )
                )
            )
        }
    }

    /**
     * Simulates analyzing the style profile.
     */
    fun analyzeStyle(imageUri: String): Flow<Result<DetectedStyle>> = flow {
        delay(1000)
        if (imageUri.isBlank()) {
            emit(Result.failure(Exception("Image URI cannot be empty")))
        } else {
            emit(
                Result.success(
                    DetectedStyle(
                        styleName = "Streetwear",
                        confidence = 0.94f,
                        mainPalette = listOf("Black", "Cream", "Grey"),
                        occasion = "Casual"
                    )
                )
            )
        }
    }
}
