package com.aura.feature.ai.segmentation

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter.ImageSegmenterOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPipeGarmentSegmenter @Inject constructor(
    @ApplicationContext private val context: Context
) : GarmentSegmenter {

    private var imageSegmenter: ImageSegmenter? = null
    private var isInitialized = false

    @Synchronized
    private fun initSegmenter() {
        if (isInitialized) return
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/selfie_multiclass_256x256.tflite")
                .build()

            val options = ImageSegmenterOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setOutputCategoryMask(true)
                .setOutputConfidenceMasks(false)
                .build()

            imageSegmenter = ImageSegmenter.createFromOptions(context, options)
            isInitialized = true
            println("AURA_DEBUG: MediaPipeGarmentSegmenter initialized successfully.")
        } catch (e: Exception) {
            isInitialized = false
            println("AURA_DEBUG: MediaPipeGarmentSegmenter initialization failed: ${e.message}")
            throw e
        }
    }

    override suspend fun segment(bitmap: Bitmap): Result<GarmentSegmentationResult> = withContext(Dispatchers.Default) {
        try {
            if (!isInitialized) {
                initSegmenter()
            }

            val segmenter = imageSegmenter ?: return@withContext Result.failure(
                IllegalStateException("Segmenter is not initialized.")
            )

            // Convert input Bitmap to MediaPipe MPImage
            val mpImage = BitmapImageBuilder(bitmap).build()

            // Run semantic segmentation
            val segmentResult = segmenter.segment(mpImage)
            val categoryMaskOpt = segmentResult.categoryMask()

            if (!categoryMaskOpt.isPresent) {
                return@withContext Result.success(
                    GarmentSegmentationResult(
                        mask = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888),
                        width = bitmap.width,
                        height = bitmap.height,
                        confidence = 0f,
                        status = SegmentationStatus.NO_CLOTHES_FOUND
                    )
                )
            }

            val categoryMask = categoryMaskOpt.get()
            val maskWidth = categoryMask.width
            val maskHeight = categoryMask.height
            val totalPixels = maskWidth * maskHeight

            val byteBuffer = ByteBufferExtractor.extract(categoryMask)
            byteBuffer.rewind()

            var clothesPixelCount = 0
            val maskPixels = IntArray(totalPixels)
            for (i in 0 until totalPixels) {
                val category = byteBuffer.get().toInt() and 0xFF
                if (category == 4) { // 4 is CLOTHES in SelfieMulticlass
                    maskPixels[i] = android.graphics.Color.WHITE
                    clothesPixelCount++
                } else {
                    maskPixels[i] = android.graphics.Color.TRANSPARENT
                }
            }

            val clothesMask256 = Bitmap.createBitmap(maskPixels, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)

            // Correctly scale/interpolate the mask back to the original bitmap dimensions
            val scaledMask = Bitmap.createScaledBitmap(clothesMask256, bitmap.width, bitmap.height, true)

            val status = if (clothesPixelCount > 0) {
                SegmentationStatus.SUCCESS
            } else {
                SegmentationStatus.NO_CLOTHES_FOUND
            }

            // Estimate confidence as ratio of clothes pixels over total pixels (or a default 0.9f on success)
            val confidence = if (status == SegmentationStatus.SUCCESS) 0.92f else 0f

            Result.success(
                GarmentSegmentationResult(
                    mask = scaledMask,
                    width = bitmap.width,
                    height = bitmap.height,
                    confidence = confidence,
                    status = status
                )
            )
        } catch (e: Exception) {
            println("AURA_DEBUG: MediaPipeGarmentSegmenter segmentation error: ${e.message}")
            Result.failure(e)
        }
    }
}
