package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.feature.ai.model.Detection
import com.aura.feature.ai.repository.FashionVisionRepository
import javax.inject.Inject

class AnalyzeOutfitUseCase @Inject constructor(
    private val repository: FashionVisionRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): List<Detection> {
        return repository.detectClothing(bitmap)
    }
}
