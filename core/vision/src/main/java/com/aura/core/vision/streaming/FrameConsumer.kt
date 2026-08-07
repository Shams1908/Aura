package com.aura.core.vision.streaming

import com.aura.core.vision.model.VisionFrame

interface FrameConsumer {
    suspend fun consume(frame: VisionFrame)
}
