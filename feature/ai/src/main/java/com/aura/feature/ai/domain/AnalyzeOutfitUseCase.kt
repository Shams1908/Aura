package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.feature.ai.model.OutfitAnalysis
import com.aura.feature.ai.repository.FashionVisionRepository
import javax.inject.Inject

class AnalyzeOutfitUseCase @Inject constructor(
    private val repository: FashionVisionRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): OutfitAnalysis {
        return repository.analyze(bitmap)
    }
}
