package com.aura.core.vision.streaming

import com.aura.core.vision.model.VisionFrame
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FrameDispatcher(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend fun dispatch(frame: VisionFrame, consumer: FrameConsumer) {
        withContext(dispatcher) {
            consumer.consume(frame)
        }
    }
}
