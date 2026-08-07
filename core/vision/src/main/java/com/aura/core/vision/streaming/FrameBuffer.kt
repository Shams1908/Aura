package com.aura.core.vision.streaming

import android.graphics.Bitmap
import com.aura.core.vision.model.VisionFrame
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FrameBuffer {
    private val lock = ReentrantLock()
    private var recycledBitmap: Bitmap? = null

    /**
     * Obtains a Bitmap of the specified dimensions, reusing a recycled one if available.
     */
    fun obtainBitmap(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        lock.withLock {
            val recycled = recycledBitmap
            if (recycled != null && recycled.width == width && recycled.height == height && recycled.config == config) {
                recycledBitmap = null
                return recycled
            }
            return Bitmap.createBitmap(width, height, config)
        }
    }

    /**
     * Recycles the given frame's bitmap if it is no longer needed, storing it for future frames.
     */
    fun recycle(frame: VisionFrame) {
        lock.withLock {
            val bitmap = frame.bitmap
            if (bitmap.isMutable) {
                recycledBitmap = bitmap
            }
        }
    }

    fun clear() {
        lock.withLock {
            recycledBitmap = null
        }
    }
}
