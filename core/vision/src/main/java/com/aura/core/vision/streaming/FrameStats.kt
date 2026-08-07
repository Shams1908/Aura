package com.aura.core.vision.streaming

data class FrameStats(
    val inputFps: Double = 0.0,
    val processingFps: Double = 0.0,
    val droppedFramesCount: Long = 0L,
    val pipelineLatencyMs: Long = 0L,
    val processingState: ProcessingState = ProcessingState.Idle
)
