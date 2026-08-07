package com.aura.core.vision.streaming

import com.aura.core.vision.model.VisionFrame
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface FrameQueue {
    fun enqueue(frame: VisionFrame): Boolean
    fun dequeue(): VisionFrame?
    fun clear()
    val size: Int
    val droppedCount: Long
}

class CircularFrameQueue(private val capacity: Int = 1) : FrameQueue {
    private val lock = ReentrantLock()
    private val array = arrayOfNulls<VisionFrame>(capacity)
    private var head = 0
    private var tail = 0
    private var count = 0
    private var _droppedCount = 0L

    override val size: Int
        get() = lock.withLock { count }

    override val droppedCount: Long
        get() = lock.withLock { _droppedCount }

    override fun enqueue(frame: VisionFrame): Boolean {
        lock.withLock {
            if (count == capacity) {
                // Drop head (the oldest frame) to make space
                array[head] = null
                head = (head + 1) % capacity
                count--
                _droppedCount++
            }
            array[tail] = frame
            tail = (tail + 1) % capacity
            count++
            return true
        }
    }

    override fun dequeue(): VisionFrame? {
        lock.withLock {
            if (count == 0) return null
            val frame = array[head]
            array[head] = null
            head = (head + 1) % capacity
            count--
            return frame
        }
    }

    override fun clear() {
        lock.withLock {
            for (i in array.indices) {
                array[i] = null
            }
            head = 0
            tail = 0
            count = 0
            _droppedCount = 0L
        }
    }
}
