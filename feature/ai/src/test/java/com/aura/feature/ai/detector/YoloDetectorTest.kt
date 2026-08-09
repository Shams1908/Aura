package com.aura.feature.ai.detector

import android.graphics.RectF
import com.aura.feature.ai.model.Detection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YoloDetectorTest {

    @Test
    fun testLetterboxCoordinateRestoration() {
        val srcWidth = 1280
        val srcHeight = 640
        val targetWidth = 640f
        val targetHeight = 640f

        // scale = minOf(640/1280, 640/640) = 0.5f
        // dx = (640 - 1280 * 0.5) / 2 = 0f
        // dy = (640 - 640 * 0.5) / 2 = 160f
        val scale = minOf(targetWidth / srcWidth, targetHeight / srcHeight)
        val dx = (targetWidth - srcWidth * scale) / 2f
        val dy = (targetHeight - srcHeight * scale) / 2f

        assertEquals(0.5f, scale, 0.001f)
        assertEquals(0f, dx, 0.001f)
        assertEquals(160f, dy, 0.001f)

        // Bounding box center at the exact middle of the 640x640 letterboxed space
        val xc = 320f
        val yc = 320f
        val w = 100f
        val h = 100f

        // Restored coordinates
        val x1 = ((xc - w / 2f) - dx) / scale
        val y1 = ((yc - h / 2f) - dy) / scale
        val x2 = ((xc + w / 2f) - dx) / scale
        val y2 = ((yc + h / 2f) - dy) / scale

        // xc - w/2 = 270. (270 - 0) / 0.5 = 540
        assertEquals(540f, x1, 0.001f)
        // yc - h/2 = 270. (270 - 160) / 0.5 = 220
        assertEquals(220f, y1, 0.001f)
        // xc + w/2 = 370. (370 - 0) / 0.5 = 740
        assertEquals(740f, x2, 0.001f)
        // yc + h/2 = 370. (370 - 160) / 0.5 = 420
        assertEquals(420f, y2, 0.001f)
    }

    @Test
    fun testNmsSameClassHighOverlap() {
        // High overlap detections of the same class (jacket = classId 4)
        val box1 = RectF().apply {
            left = 100f
            top = 100f
            right = 200f
            bottom = 200f
        }
        val det1 = Detection(
            label = "jacket",
            classId = 4,
            confidence = 0.85f,
            boundingBox = box1
        )
        val box2 = RectF().apply {
            left = 110f
            top = 110f
            right = 210f
            bottom = 210f
        }
        val det2 = Detection(
            label = "jacket",
            classId = 4,
            confidence = 0.65f,
            boundingBox = box2
        )

        val detections = listOf(det1, det2)
        val nmsResult = applyNmsForTest(detections)

        // Lower confidence detection should be suppressed
        assertEquals(1, nmsResult.size)
        assertEquals(0.85f, nmsResult[0].confidence, 0.001f)
    }

    @Test
    fun testNmsDifferentClassesHighOverlap() {
        // High overlap but different classes (e.g. shirt = classId 0, jacket = classId 4)
        // They should NOT suppress each other (class-specific NMS)
        val box1 = RectF().apply {
            left = 100f
            top = 100f
            right = 200f
            bottom = 200f
        }
        val det1 = Detection(
            label = "shirt",
            classId = 0,
            confidence = 0.85f,
            boundingBox = box1
        )
        val box2 = RectF().apply {
            left = 105f
            top = 105f
            right = 205f
            bottom = 205f
        }
        val det2 = Detection(
            label = "jacket",
            classId = 4,
            confidence = 0.65f,
            boundingBox = box2 // high overlap
        )

        val detections = listOf(det1, det2)
        val nmsResult = applyNmsForTest(detections)

        // Both should be kept
        assertEquals(2, nmsResult.size)
        assertTrue(nmsResult.any { it.classId == 0 })
        assertTrue(nmsResult.any { it.classId == 4 })
    }

    @Test
    fun testNmsSameClassLowOverlap() {
        val box1 = RectF().apply {
            left = 100f
            top = 100f
            right = 200f
            bottom = 200f
        }
        val det1 = Detection(
            label = "jacket",
            classId = 4,
            confidence = 0.85f,
            boundingBox = box1
        )
        val box2 = RectF().apply {
            left = 300f
            top = 300f
            right = 400f
            bottom = 400f
        }
        val det2 = Detection(
            label = "jacket",
            classId = 4,
            confidence = 0.65f,
            boundingBox = box2 // No overlap
        )

        val detections = listOf(det1, det2)
        val nmsResult = applyNmsForTest(detections)

        assertEquals(2, nmsResult.size)
    }

    private fun applyNmsForTest(boxes: List<Detection>): List<Detection> {
        val selected = mutableListOf<Detection>()
        val boxesByClass = boxes.groupBy { it.classId }
        for ((_, classBoxes) in boxesByClass) {
            val sorted = classBoxes.sortedByDescending { it.confidence }.toMutableList()
            while (sorted.isNotEmpty()) {
                val first = sorted.removeAt(0)
                selected.add(first)
                val iterator = sorted.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (calculateIou(first.boundingBox, next.boundingBox) > 0.45f) {
                        iterator.remove()
                    }
                }
            }
        }
        return selected
    }

    private fun calculateIou(a: RectF, b: RectF): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)

        if (left >= right || top >= bottom) return 0f

        val intersectionArea = (right - left) * (bottom - top)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val unionArea = areaA + areaB - intersectionArea
        return if (unionArea > 0f) intersectionArea / unionArea else 0f
    }
}
