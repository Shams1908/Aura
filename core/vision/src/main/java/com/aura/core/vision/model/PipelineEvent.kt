package com.aura.core.vision.model

sealed interface PipelineEvent {
    object Started : PipelineEvent
    data class FrameProcessed(val result: PipelineResult) : PipelineEvent
    data class ErrorOccurred(val error: Throwable) : PipelineEvent
    object Finished : PipelineEvent
}
