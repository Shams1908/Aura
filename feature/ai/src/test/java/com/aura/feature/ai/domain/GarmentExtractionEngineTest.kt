package com.aura.feature.ai.domain

import android.graphics.Bitmap
import android.graphics.RectF
import com.aura.core.common.data.ReferenceImage
import com.aura.core.common.data.ReferenceImageMetadata
import com.aura.core.common.data.ReferenceImageSource
import com.aura.feature.ai.detector.YoloDetector
import com.aura.feature.ai.inference.AIError
import com.aura.feature.ai.model.Detection
import com.aura.feature.ai.segmentation.GarmentSegmenter
import com.aura.feature.ai.segmentation.GarmentSegmentationResult
import com.aura.feature.ai.segmentation.SegmentationStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.MockedStatic
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.doAnswer

@OptIn(ExperimentalCoroutinesApi::class)
class GarmentExtractionEngineTest {

    private val yoloDetector: YoloDetector = mock(YoloDetector::class.java)
    private val garmentSegmenter: GarmentSegmenter = mock(GarmentSegmenter::class.java)
    private lateinit var mockBitmap: Bitmap
    private lateinit var mockMaskBitmap: Bitmap
    private lateinit var mockCropBitmap: Bitmap

    private lateinit var extractionEngine: GarmentExtractionEngineImpl

    private val testReferenceImage = ReferenceImage(
        uri = "content://media/external/images/media/1",
        source = ReferenceImageSource.USER_DEVICE_GALLERY,
        metadata = ReferenceImageMetadata(title = "tshirt.jpg", sizeBytes = 1024L)
    )

    // Helper matchers to prevent Kotlin null-checks throwing NPE during Mockito stubbing
    private fun anyIntArray(): IntArray {
        any<IntArray>()
        return intArrayOf()
    }

    private fun anyConfig(): Bitmap.Config {
        any<Bitmap.Config>()
        return Bitmap.Config.ARGB_8888
    }

    private fun anyBitmap(): Bitmap {
        any<Bitmap>()
        return mockBitmap
    }

    @Before
    fun setUp() {
        mockBitmap = mock(Bitmap::class.java)
        mockMaskBitmap = mock(Bitmap::class.java)
        mockCropBitmap = mock(Bitmap::class.java)

        extractionEngine = GarmentExtractionEngineImpl(yoloDetector, garmentSegmenter)

        // Stub standard mockBitmap behavior
        `when`(mockBitmap.width).thenReturn(640)
        `when`(mockBitmap.height).thenReturn(640)
    }

    @After
    fun tearDown() {
        // No-op as static mocks are isolated to test blocks
    }

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
            boundingBox = RectF().apply {
                left = 10f
                top = 10f
                right = 100f
                bottom = 100f
            }
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(lowConfDetection))

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AIError.LowConfidence, result.exceptionOrNull())
    }

    @Test
    fun testExtractGarmentSegmenterUnavailable() = runTest {
        // Given
        val highConfDetection = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.85f,
            boundingBox = RectF().apply {
                left = 10f
                top = 10f
                right = 100f
                bottom = 100f
            }
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(highConfDetection))
        `when`(garmentSegmenter.segment(mockBitmap)).thenReturn(Result.failure(Exception("Init error")))

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AIError.ModelUnavailable, result.exceptionOrNull())
    }

    @Test
    fun testExtractGarmentEmptyClothesMask() = runTest {
        // Given
        val highConfDetection = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.85f,
            boundingBox = RectF().apply {
                left = 10f
                top = 10f
                right = 100f
                bottom = 100f
            }
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(highConfDetection))
        `when`(garmentSegmenter.segment(mockBitmap)).thenReturn(
            Result.success(
                GarmentSegmentationResult(
                    mask = mockMaskBitmap,
                    width = 640,
                    height = 640,
                    confidence = 0f,
                    status = SegmentationStatus.NO_CLOTHES_FOUND
                )
            )
        )

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AIError.SegmentationFailed, result.exceptionOrNull())
    }

    @Test
    fun testExtractGarmentSuccess() = runTest {
        // Given
        val highConfDetection = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.85f,
            boundingBox = RectF().apply {
                left = 10f
                top = 10f
                right = 100f
                bottom = 100f
            }
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(highConfDetection))
        `when`(garmentSegmenter.segment(mockBitmap)).thenReturn(
            Result.success(
                GarmentSegmentationResult(
                    mask = mockMaskBitmap,
                    width = 640,
                    height = 640,
                    confidence = 0.92f,
                    status = SegmentationStatus.SUCCESS
                )
            )
        )

        // Setup mockMaskBitmap to return a dummy clothes pixel inside the detection box
        // Bounding box is [10, 10, 100, 100]. We stub getPixels to simulate a clothes pixel at (20, 20)
        doAnswer { invocation ->
            val pixels = invocation.getArgument<IntArray>(0)
            pixels[20 * 640 + 20] = android.graphics.Color.WHITE
            null
        }.`when`(mockMaskBitmap).getPixels(
            anyIntArray(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt()
        )

        // Isolate static mock inside success test block only
        val bitmapMockedStatic = mockStatic(Bitmap::class.java)
        try {
            bitmapMockedStatic.`when`<Bitmap> {
                Bitmap.createBitmap(
                    anyIntArray(),
                    anyInt(),
                    anyInt(),
                    anyConfig()
                )
            }.thenReturn(mockCropBitmap)

            // When
            val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

            // Then
            assertTrue(result.isSuccess)
            val asset = result.getOrNull()
            assertTrue(asset != null)
            assertEquals("shirt", asset!!.category)
            assertEquals(0.85f, asset.confidence, 0.001f)
            assertEquals(testReferenceImage.uri, asset.sourceReferenceUri)
            assertEquals(mockCropBitmap, asset.texture)
            assertEquals(mockCropBitmap, asset.alphaMask)
        } finally {
            bitmapMockedStatic.close()
        }
    }

    @Test
    fun testExtractGarmentEmptyIntersection() = runTest {
        // Given
        val highConfDetection = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.85f,
            boundingBox = RectF().apply {
                left = 10f
                top = 10f
                right = 100f
                bottom = 100f
            }
        )
        `when`(yoloDetector.predict(mockBitmap)).thenReturn(listOf(highConfDetection))
        `when`(garmentSegmenter.segment(mockBitmap)).thenReturn(
            Result.success(
                GarmentSegmentationResult(
                    mask = mockMaskBitmap,
                    width = 640,
                    height = 640,
                    confidence = 0.92f,
                    status = SegmentationStatus.SUCCESS
                )
            )
        )

        // Setup mask with pixel outside of bounding box [10, 10, 100, 100] (e.g. at 200, 200)
        doAnswer { invocation ->
            val pixels = invocation.getArgument<IntArray>(0)
            pixels[200 * 640 + 200] = android.graphics.Color.WHITE
            null
        }.`when`(mockMaskBitmap).getPixels(
            anyIntArray(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt()
        )

        // When
        val result = extractionEngine.extractGarment(testReferenceImage, mockBitmap)

        // Then
        assertTrue(result.isFailure)
        assertEquals(AIError.SegmentationFailed, result.exceptionOrNull())
    }
}
