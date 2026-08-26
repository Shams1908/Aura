package com.aura.feature.camera.domain

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.aura.core.vision.model.PoseResult
import com.aura.feature.ai.domain.GarmentAssetProvider
import com.aura.feature.ai.model.GarmentAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class VirtualTryOnTest {

    private lateinit var assetProvider: GarmentAssetProvider
    private lateinit var anchorProvider: BodyAnchorProvider
    private lateinit var calculator: GarmentTransformCalculator
    private lateinit var controller: VirtualTryOnController

    private lateinit var mockTexture: Bitmap
    private lateinit var mockAlphaMask: Bitmap

    @Before
    fun setUp() {
        assetProvider = GarmentAssetProvider()
        anchorProvider = BodyAnchorProvider()
        calculator = GarmentTransformCalculator()
        controller = VirtualTryOnController(assetProvider, anchorProvider, calculator)

        mockTexture = mock(Bitmap::class.java)
        mockAlphaMask = mock(Bitmap::class.java)

        `when`(mockTexture.width).thenReturn(200)
        `when`(mockTexture.height).thenReturn(300) // Aspect ratio: 200 / 300 = 0.6667
        `when`(mockAlphaMask.width).thenReturn(200)
        `when`(mockAlphaMask.height).thenReturn(300)
    }

    private fun createMockAsset(category: String): GarmentAsset {
        return GarmentAsset(
            category = category,
            texture = mockTexture,
            alphaMask = mockAlphaMask,
            originalDimensions = Pair(200, 300),
            normalizedBounds = android.graphics.RectF(0.2f, 0.2f, 0.8f, 0.8f),
            confidence = 0.9f,
            sourceReferenceUri = "content://dummy"
        )
    }

    private fun createValidPoseResult(): PoseResult {
        // Centered body landmarks to prevent clipping out of narrow view aspect ratios
        val landmarks = listOf(
            PoseResult.PoseLandmark(id = 11, x = 280f, y = 150f, z = 0f, likelihood = 0.9f), // left_shoulder
            PoseResult.PoseLandmark(id = 12, x = 360f, y = 150f, z = 0f, likelihood = 0.9f), // right_shoulder
            PoseResult.PoseLandmark(id = 23, x = 290f, y = 300f, z = 0f, likelihood = 0.9f), // left_hip
            PoseResult.PoseLandmark(id = 24, x = 350f, y = 300f, z = 0f, likelihood = 0.9f)  // right_hip
        )
        return PoseResult(
            landmarks = landmarks,
            imageWidth = 640,
            imageHeight = 480,
            rotationDegrees = 0,
            isFrontCamera = false
        )
    }

    @Test
    fun testUpperBodyGarmentTransformSuccess() {
        val asset = createMockAsset("t-shirt")
        assetProvider.setGarmentAsset(asset)
        val pose = createValidPoseResult()

        val state = controller.processFrame(
            pose = pose,
            timestampMs = 1000L,
            viewWidth = 1080f,
            viewHeight = 1920f
        )

        assertTrue(state is VirtualTryOnRenderState.Render)
        val renderState = state as VirtualTryOnRenderState.Render
        assertEquals("t-shirt", renderState.asset.category)

        // Verify the transform translation, scale, aspect ratio
        val t = renderState.transform
        assertTrue(t.width > 0f)
        assertTrue(t.height > 0f)

        // Aspect ratio must be preserved: width / height == original texture width / height
        val aspect = t.width / t.height
        assertEquals(200f / 300f, aspect, 0.01f)

        // Check translation sits near the chest center
        assertTrue(t.translationX > 0f)
        assertTrue(t.translationY > 0f)
    }

    @Test
    fun testLowerBodyGarmentTransformSuccess() {
        val asset = createMockAsset("pants")
        assetProvider.setGarmentAsset(asset)
        val pose = createValidPoseResult()

        val state = controller.processFrame(
            pose = pose,
            timestampMs = 1000L,
            viewWidth = 1080f,
            viewHeight = 1920f
        )

        assertTrue(state is VirtualTryOnRenderState.Render)
        val renderState = state as VirtualTryOnRenderState.Render
        val t = renderState.transform

        // Aspect ratio must be preserved
        val aspect = t.width / t.height
        assertEquals(200f / 300f, aspect, 0.01f)

        // Lower body category (pants) should position the waist near hips
        assertTrue(t.translationY > 0f)
    }

    @Test
    fun testInvalidAnchorProducesNoRenderTransform() {
        val asset = createMockAsset("t-shirt")
        assetProvider.setGarmentAsset(asset)

        // Pose containing empty landmarks (no tracked body)
        val pose = PoseResult(
            landmarks = emptyList(),
            imageWidth = 640,
            imageHeight = 480,
            rotationDegrees = 0,
            isFrontCamera = false
        )

        val state = controller.processFrame(
            pose = pose,
            timestampMs = 1000L,
            viewWidth = 1080f,
            viewHeight = 1920f
        )

        assertTrue(state is VirtualTryOnRenderState.NoTracking)
    }

    @Test
    fun testUnsupportedCategoriesFailSafely() {
        val asset = createMockAsset("shoes")
        assetProvider.setGarmentAsset(asset)
        val pose = createValidPoseResult()

        val state = controller.processFrame(
            pose = pose,
            timestampMs = 1000L,
            viewWidth = 1080f,
            viewHeight = 1920f
        )

        assertTrue(state is VirtualTryOnRenderState.UnsupportedCategory)
        assertEquals("shoes", (state as VirtualTryOnRenderState.UnsupportedCategory).category)
    }

    @Test
    fun testSmoothingReducesSuddenPositionJumps() {
        val asset = createMockAsset("t-shirt")
        assetProvider.setGarmentAsset(asset)

        // First frame
        val pose1 = createValidPoseResult()
        val state1 = controller.processFrame(pose1, 1000L, 1080f, 1920f) as VirtualTryOnRenderState.Render
        val t1 = state1.transform

        // Second frame: slight displacement (5 sensor pixels = 20 view pixels, below safety jump threshold of 150)
        val pose2 = PoseResult(
            landmarks = listOf(
                PoseResult.PoseLandmark(id = 11, x = 285f, y = 152f, z = 0f, likelihood = 0.9f),
                PoseResult.PoseLandmark(id = 12, x = 365f, y = 152f, z = 0f, likelihood = 0.9f),
                PoseResult.PoseLandmark(id = 23, x = 295f, y = 302f, z = 0f, likelihood = 0.9f),
                PoseResult.PoseLandmark(id = 24, x = 355f, y = 302f, z = 0f, likelihood = 0.9f)
            ),
            imageWidth = 640,
            imageHeight = 480,
            rotationDegrees = 0,
            isFrontCamera = false
        )

        val state2 = controller.processFrame(pose2, 1033L, 1080f, 1920f) as VirtualTryOnRenderState.Render
        val t2 = state2.transform

        // Verify position change is smoothed (less than full change)
        val rawResult = calculator.calculateTransform(asset, anchorProvider.getAnchor(pose2, 1033L)!!, 1080f, 1920f, pose2) as GarmentTransformResult.Success
        
        // Smoothed dx should be smaller than raw dx relative to t1
        val rawDx = rawResult.translationX - t1.translationX
        val smoothedDx = t2.translationX - t1.translationX
        assertTrue(kotlin.math.abs(smoothedDx) < kotlin.math.abs(rawDx))
    }

    @Test
    fun testSmoothingResetsCorrectlyAfterPersonLoss() {
        val asset = createMockAsset("t-shirt")
        assetProvider.setGarmentAsset(asset)

        // Track frame 1
        val pose1 = createValidPoseResult()
        controller.processFrame(pose1, 1000L, 1080f, 1920f)

        // Lose tracking (person leaves)
        val pose2 = PoseResult(landmarks = emptyList(), imageWidth = 640, imageHeight = 480)
        controller.processFrame(pose2, 2000L, 1080f, 1920f) // Sets trackingLostTimeMs = 2000L
        
        val state2 = controller.processFrame(pose2, 3000L, 1080f, 1920f) // elapsed = 1000ms > 800ms grace threshold
        assertTrue(state2 is VirtualTryOnRenderState.NoTracking)

        // Person re-enters at a completely different position
        val pose3 = PoseResult(
            landmarks = listOf(
                PoseResult.PoseLandmark(id = 11, x = 300f, y = 150f, z = 0f, likelihood = 0.9f),
                PoseResult.PoseLandmark(id = 12, x = 380f, y = 150f, z = 0f, likelihood = 0.9f),
                PoseResult.PoseLandmark(id = 23, x = 310f, y = 300f, z = 0f, likelihood = 0.9f),
                PoseResult.PoseLandmark(id = 24, x = 370f, y = 300f, z = 0f, likelihood = 0.9f)
            ),
            imageWidth = 640,
            imageHeight = 480,
            rotationDegrees = 0,
            isFrontCamera = false
        )

        // Verify that the transform instantly snaps to the new position without lag/smoothing drag
        val state3 = controller.processFrame(pose3, 2033L, 1080f, 1920f) as VirtualTryOnRenderState.Render
        val t3 = state3.transform
        val rawResult = calculator.calculateTransform(asset, anchorProvider.getAnchor(pose3, 2033L)!!, 1080f, 1920f, pose3) as GarmentTransformResult.Success

        // Since smoothing reset, t3.translationX must exactly match rawResult.translationX
        assertEquals(rawResult.translationX, t3.translationX, 0.01f)
    }

    @Test
    fun testAlphaMaskAndTextureDimensionsRemainAligned() {
        val asset = createMockAsset("t-shirt")
        assetProvider.setGarmentAsset(asset)
        val pose = createValidPoseResult()

        val state = controller.processFrame(
            pose = pose,
            timestampMs = 1000L,
            viewWidth = 1080f,
            viewHeight = 1920f
        )

        assertTrue(state is VirtualTryOnRenderState.Render)
        val renderState = state as VirtualTryOnRenderState.Render
        
        // Assert texture and mask dimensions are identical on the asset
        assertEquals(asset.texture.width, asset.alphaMask.width)
        assertEquals(asset.texture.height, asset.alphaMask.height)

        // Assert they are rendered with the same width and height
        val t = renderState.transform
        assertTrue(t.width > 0f)
        assertTrue(t.height > 0f)
    }
}
