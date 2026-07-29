package com.aura.feature.ai.domain

import android.graphics.Bitmap
import kotlinx.serialization.Serializable

@Serializable
enum class GarmentType {
    UPPER_BODY, LOWER_BODY, FULL_BODY, COAT, HOODIE, JACKET, SHIRT, SKIRT, PANTS
}

data class ParsedGarment(
    val type: GarmentType,
    val mask: Bitmap,            // Alpha channel mask of the extracted clothes
    val texture: Bitmap          // Extracted RGB texture matching the boundaries
)

interface GarmentParser {
    suspend fun parseGarment(
        sourceImage: Bitmap,
        targetType: GarmentType
    ): Result<ParsedGarment>
}
