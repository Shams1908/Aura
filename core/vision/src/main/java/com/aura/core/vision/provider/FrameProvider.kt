package com.aura.core.vision.provider

import com.aura.core.vision.model.VisionFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

interface FrameProvider {
    val frames: Flow<VisionFrame>
    fun emitFrame(frame: VisionFrame)
}

@Singleton
class DefaultFrameProvider @Inject constructor() : FrameProvider {
    private val _frames = MutableSharedFlow<VisionFrame>(replay = 0, extraBufferCapacity = 16)
    override val frames: SharedFlow<VisionFrame> = _frames.asSharedFlow()

    override fun emitFrame(frame: VisionFrame) {
        _frames.tryEmit(frame)
    }
}
