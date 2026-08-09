package com.aura.feature.ai.preprocessing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagePreprocessor @Inject constructor() {

    /**
     * Resizes a bitmap to target dimensions, keeping aspect ratio using letterbox padding.
     */
    fun resize(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        val scale = minOf(
            targetWidth.toFloat() / bitmap.width,
            targetHeight.toFloat() / bitmap.height
        )
        
        val dx = (targetWidth - bitmap.width * scale) / 2f
        val dy = (targetHeight - bitmap.height * scale) / 2f
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(dx, dy)
        }
        
        canvas.drawBitmap(bitmap, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        return result
    }

    fun centerCrop(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val sourceWidth = bitmap.width
        val sourceHeight = bitmap.height
        
        val scale = maxOf(
            targetWidth.toFloat() / sourceWidth,
            targetHeight.toFloat() / sourceHeight
        )
        
        val scaledWidth = scale * sourceWidth
        val scaledHeight = scale * sourceHeight
        
        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f
        
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(left, top)
        }
        canvas.drawBitmap(bitmap, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        return result
    }

    /**
     * Normalizes color channels using mean and standard deviation.
     */
    fun normalize(bitmap: Bitmap, mean: FloatArray, std: FloatArray): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val result = FloatArray(3 * width * height)
        for (i in 0 until width * height) {
            val pix = pixels[i]
            val r = ((pix shr 16) and 0xFF) / 255f
            val g = ((pix shr 8) and 0xFF) / 255f
            val b = (pix and 0xFF) / 255f
            
            result[i] = (r - mean[0]) / std[0]
            result[width * height + i] = (g - mean[1]) / std[1]
            result[2 * width * height + i] = (b - mean[2]) / std[2]
        }
        return result
    }

    fun rotateIfRequired(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Preprocesses a bitmap for YOLOv8 model inputs (NCHW, RGB scaled [0..1]).
     */
    fun preprocessForYolo(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): FloatBuffer {
        val resized = resize(bitmap, targetWidth, targetHeight)
        val pixels = IntArray(targetWidth * targetHeight)
        resized.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
        
        val buffer = ByteBuffer.allocateDirect(1 * 3 * targetWidth * targetHeight * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        
        val rOffset = 0
        val gOffset = targetWidth * targetHeight
        val bOffset = 2 * targetWidth * targetHeight
        
        for (i in 0 until targetWidth * targetHeight) {
            val pix = pixels[i]
            val r = ((pix shr 16) and 0xFF) / 255f
            val g = ((pix shr 8) and 0xFF) / 255f
            val b = (pix and 0xFF) / 255f
            
            buffer.put(rOffset + i, r)
            buffer.put(gOffset + i, g)
            buffer.put(bOffset + i, b)
        }
        
        buffer.rewind()
        return buffer
    }

    fun bitmapToTensor(bitmap: Bitmap): ByteBuffer {
        val width = bitmap.width
        val height = bitmap.height
        val buffer = ByteBuffer.allocateDirect(4 * width * height * 3)
            .order(ByteOrder.nativeOrder())
        return buffer
    }
}
