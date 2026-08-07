package com.aura.core.vision.pipeline

import com.aura.core.vision.model.VisionFrame
import com.aura.core.vision.model.PipelineResult

interface FrameProcessor {
    suspend fun process(frame: VisionFrame): PipelineResult
}
