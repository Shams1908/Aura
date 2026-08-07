package com.aura.core.vision.streaming

import com.aura.core.vision.model.VisionFrame
import com.aura.core.vision.pipeline.VisionPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameStreamManager @Inject constructor(
    private val visionPipeline: VisionPipeline
) {
    private val queue = CircularFrameQueue(capacity = 1)
    private val dispatcher = FrameDispatcher()
    private val monitor = FrameRateMonitor()
    val buffer = FrameBuffer()

    private val _stats = MutableStateFlow(FrameStats())
    val stats: StateFlow<FrameStats> = _stats.asStateFlow()

    private val consumer = object : FrameConsumer {
        override suspend fun consume(frame: VisionFrame) {
            try {
                visionPipeline.processFrame(frame)
            } finally {
                buffer.recycle(frame)
            }
        }
    }

    private val scheduler = FrameScheduler(
        queue = queue,
        dispatcher = dispatcher,
        consumer = consumer,
        onStatsUpdated = { latency ->
            monitor.recordProcessing(latency)
            updateStats()
        },
        onStateChanged = { state ->
            _stats.update { it.copy(processingState = state) }
        }
    )

    fun start(scope: CoroutineScope) {
        monitor.clear()
        queue.clear()
        buffer.clear()
        scheduler.start(scope)
    }

    fun stop() {
        scheduler.stop()
        monitor.clear()
        queue.clear()
        buffer.clear()
        _stats.update {
            FrameStats(
                inputFps = 0.0,
                processingFps = 0.0,
                droppedFramesCount = 0L,
                pipelineLatencyMs = 0L,
                processingState = ProcessingState.Idle
            )
        }
    }

    fun submitFrame(frame: VisionFrame) {
        monitor.recordInput()
        val enqueued = queue.enqueue(frame)
        if (!enqueued) {
            buffer.recycle(frame)
        }
        updateStats()
    }

    private fun updateStats() {
        _stats.update {
            it.copy(
                inputFps = monitor.getInputFps(),
                processingFps = monitor.getProcessingFps(),
                droppedFramesCount = queue.droppedCount,
                pipelineLatencyMs = monitor.getAverageLatencyMs()
            )
        }
    }
}
