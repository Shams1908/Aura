package com.aura.feature.ai.repository

import android.graphics.Bitmap
import com.aura.feature.ai.detector.YoloDetector
import com.aura.feature.ai.model.Detection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FashionVisionRepositoryImpl @Inject constructor(
    private val yoloDetector: YoloDetector
) : FashionVisionRepository {

    override suspend fun detectClothing(bitmap: Bitmap): List<Detection> {
        return withContext(Dispatchers.Default) {
            yoloDetector.load()
            yoloDetector.predict(bitmap)
        }
    }
}
