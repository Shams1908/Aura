package com.aura.core.vision.model

sealed interface PipelineState {
    object Idle : PipelineState
    object ReceivingFrames : PipelineState
    object Processing : PipelineState
    object Completed : PipelineState
    data class Error(val exception: Throwable) : PipelineState
}
