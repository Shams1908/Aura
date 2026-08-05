package com.aura.feature.ai.domain

import android.graphics.Bitmap
import javax.inject.Inject

class MockGarmentParserImpl @Inject constructor() : GarmentParser {
    override suspend fun parseGarment(
        sourceImage: Bitmap,
        targetType: GarmentType
    ): Result<ParsedGarment> {
        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return Result.success(
            ParsedGarment(
                type = targetType,
                mask = emptyBitmap,
                texture = emptyBitmap
            )
        )
    }
}
