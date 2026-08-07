package com.aura.core.vision.streaming

enum class ProcessingState {
    Idle,
    Streaming,
    ProcessingFrame,
    Paused,
    Error
}
