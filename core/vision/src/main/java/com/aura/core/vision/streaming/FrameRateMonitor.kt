package com.aura.core.vision.streaming

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FrameRateMonitor(private val windowSize: Int = 30) {
    private val lock = ReentrantLock()
    private val inputTimestamps = LongArray(windowSize)
    private var inputCount = 0
    private val processTimestamps = LongArray(windowSize)
    private var processCount = 0

    private val latencyHistory = LongArray(windowSize)
    private var latencyCount = 0

    fun recordInput() {
        lock.withLock {
            inputTimestamps[inputCount % windowSize] = System.currentTimeMillis()
            inputCount++
        }
    }

    fun recordProcessing(latencyMs: Long) {
        lock.withLock {
            val now = System.currentTimeMillis()
            processTimestamps[processCount % windowSize] = now
            processCount++

            latencyHistory[latencyCount % windowSize] = latencyMs
            latencyCount++
        }
    }

    fun getInputFps(): Double {
        lock.withLock {
            if (inputCount < 2) return 0.0
            val currentCount = minOf(inputCount, windowSize)
            val newestIdx = (inputCount - 1) % windowSize
            val oldestIdx = if (inputCount < windowSize) 0 else inputCount % windowSize
            val timeDiff = inputTimestamps[newestIdx] - inputTimestamps[oldestIdx]
            if (timeDiff <= 0) return 0.0
            return (currentCount - 1) * 1000.0 / timeDiff
        }
    }

    fun getProcessingFps(): Double {
        lock.withLock {
            if (processCount < 2) return 0.0
            val currentCount = minOf(processCount, windowSize)
            val newestIdx = (processCount - 1) % windowSize
            val oldestIdx = if (processCount < windowSize) 0 else processCount % windowSize
            val timeDiff = processTimestamps[newestIdx] - processTimestamps[oldestIdx]
            if (timeDiff <= 0) return 0.0
            return (currentCount - 1) * 1000.0 / timeDiff
        }
    }

    fun getAverageLatencyMs(): Long {
        lock.withLock {
            if (latencyCount == 0) return 0L
            val currentCount = minOf(latencyCount, windowSize)
            var sum = 0L
            for (i in 0 until currentCount) {
                sum += latencyHistory[i]
            }
            return sum / currentCount
        }
    }

    fun clear() {
        lock.withLock {
            inputCount = 0
            processCount = 0
            latencyCount = 0
            for (i in 0 until windowSize) {
                inputTimestamps[i] = 0L
                processTimestamps[i] = 0L
                latencyHistory[i] = 0L
            }
        }
    }
}
