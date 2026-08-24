package com.aura.feature.ai.domain

import android.graphics.Bitmap
import android.graphics.RectF
import com.aura.core.common.data.ReferenceImage
import com.aura.core.common.data.ReferenceImageMetadata
import com.aura.core.common.data.ReferenceImageSource
import com.aura.feature.ai.detector.YoloDetector
import com.aura.feature.ai.inference.AIError
import com.aura.feature.ai.model.Detection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class GarmentExtractionEngineTest {

    private val yoloDetector: YoloDetector = mock(YoloDetector::class.java)
    private val mockBitmap: Bitmap = mock(Bitmap::class.java)
    private val extractionEngine = GarmentExtractionEngineImpl(yoloDetector)

    private val testReferenceImage = ReferenceImage(
        uri = "content://media/external/images/media/1",
        source = ReferenceImageSource.USER_DEVICE_GALLERY,
        metadata = ReferenceImageMetadata(title = "tshirt.jpg", sizeBytes = 1024L)
    )

    @Test
    fun testExtractGarmentNoDetections() = runTest {
        // Given
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(emptyList())

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        verify(yoloDetector).load()
        verify(yoloDetector).predict(mockBitmap)
        assertTrue(result.isFailure)
        assertEquals(AIError.NoGarmentDetected, result.exceptionOrNull())
    }

    @Test
    fun testExtractGarmentLowConfidence() = runTest {
        // Given
        val lowConfDetection = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.2f,
            boundingBox = RectF(10f, 10f, 100f, 100f)
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(lowConfDetection))

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AIError.LowConfidence, result.exceptionOrNull())
    }

    @Test
    fun testExtractGarmentSegmentationUnavailable() = runTest {
        // Given
        val highConfDetection = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.85f,
            boundingBox = RectF(10f, 10f, 100f, 100f)
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(highConfDetection))

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AIError.ModelUnavailable, result.exceptionOrNull())
    }
}
