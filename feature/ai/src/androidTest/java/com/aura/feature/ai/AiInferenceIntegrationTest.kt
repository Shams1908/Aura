package com.aura.feature.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aura.feature.ai.detector.YoloDetectorImpl
import com.aura.feature.ai.preprocessing.ImagePreprocessor
import com.aura.feature.ai.util.ModelLoader
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AiInferenceIntegrationTest {

    private lateinit var detector: YoloDetectorImpl
    private lateinit var modelLoader: ModelLoader
    private lateinit var preprocessor: ImagePreprocessor

    @Before
    fun setUp() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        modelLoader = ModelLoader(targetContext)
        preprocessor = ImagePreprocessor()
        detector = YoloDetectorImpl(modelLoader, preprocessor)
    }

    @After
    fun tearDown() {
        detector.close()
    }

    private fun loadTestBitmap(fileName: String): Bitmap {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val inputStream = testContext.assets.open("test_images/$fileName")
        return BitmapFactory.decodeStream(inputStream) ?: throw IllegalStateException("Failed to decode $fileName")
    }

    @Test
    fun testRealModelInferenceOnSingleGarment() {
        // 1. Load the model
        detector.load()

        // 2. Load the test image (Single jacket)
        val bitmap = loadTestBitmap("single_garment.jpg")
        assertNotNull(bitmap)

        // 3. Predict
        val detections = detector.predict(bitmap)

        // 4. Verify detections
        assertNotNull(detections)
        // Since it's a real fashion image, we expect at least one clothing detection (e.g. jacket/coat/top)
        assertTrue("Expected at least one detection on single_garment.jpg", detections.isNotEmpty())

        for (detection in detections) {
            // Label mapping should be valid
            assertNotNull(detection.label)
            assertTrue(detection.label.isNotEmpty())

            // Class ID should be between 0 and 45
            assertTrue("classId should be in [0..45]", detection.classId in 0..45)

            // Confidence must be in [0..1] and >= threshold (0.35)
            assertTrue("confidence should be >= 0.35", detection.confidence >= 0.35f)
            assertTrue("confidence should be <= 1.0", detection.confidence <= 1.0f)

            // Bounding box must be valid and within image boundaries
            val box = detection.boundingBox
            assertNotNull(box)
            assertTrue("box.left >= 0", box.left >= 0f)
            assertTrue("box.top >= 0", box.top >= 0f)
            assertTrue("box.right <= width", box.right <= bitmap.width)
            assertTrue("box.bottom <= height", box.bottom <= bitmap.height)
            assertTrue("box.left < box.right", box.left < box.right)
            assertTrue("box.top < box.bottom", box.top < box.bottom)
        }
    }

    @Test
    fun testRealModelInferenceOnMultipleGarments() {
        detector.load()
        val bitmap = loadTestBitmap("multiple_garments.jpg")
        val detections = detector.predict(bitmap)
        assertNotNull(detections)
        // Check bounds and properties of all detections
        for (detection in detections) {
            val box = detection.boundingBox
            assertTrue(box.left >= 0f && box.right <= bitmap.width)
            assertTrue(box.top >= 0f && box.bottom <= bitmap.height)
            assertTrue(detection.confidence in 0.35f..1.0f)
        }
    }
}
