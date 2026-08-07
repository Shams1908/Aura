package com.aura.feature.ai.repository

import android.graphics.Bitmap
import com.aura.feature.ai.model.OutfitAnalysis

interface FashionVisionRepository {
    suspend fun analyze(bitmap: Bitmap): OutfitAnalysis
}
