package com.aura.core.vision.streaming

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class FrameScheduler(
    private val queue: FrameQueue,
    private val dispatcher: FrameDispatcher,
    private val consumer: FrameConsumer,
    private val onStatsUpdated: (latencyMs: Long) -> Unit,
    private val onStateChanged: (ProcessingState) -> Unit
) {
    private var scope: CoroutineScope? = null
    private var loopJob: Job? = null
    private val isRunning = AtomicBoolean(false)

    fun start(parentScope: CoroutineScope) {
        if (isRunning.getAndSet(true)) return

        scope = parentScope
        onStateChanged(ProcessingState.Streaming)

        loopJob = parentScope.launch(Dispatchers.Default) {
            while (isActive) {
                val frame = queue.dequeue()
                if (frame != null) {
                    onStateChanged(ProcessingState.ProcessingFrame)
                    val startTime = System.currentTimeMillis()
                    
                    try {
                        dispatcher.dispatch(frame, consumer)
                        val latency = System.currentTimeMillis() - startTime
                        onStatsUpdated(latency)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        onStateChanged(ProcessingState.Error)
                    }
                    
                    onStateChanged(ProcessingState.Streaming)
                } else {
                    delay(10)
                }
            }
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return

        loopJob?.cancel()
        loopJob = null
        scope = null
        queue.clear()
        onStateChanged(ProcessingState.Idle)
    }
}
