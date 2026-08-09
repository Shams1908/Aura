package com.aura.feature.ai.repository

import android.graphics.Bitmap
import com.aura.feature.ai.model.Detection

interface FashionVisionRepository {
    suspend fun detectClothing(bitmap: Bitmap): List<Detection>
}
