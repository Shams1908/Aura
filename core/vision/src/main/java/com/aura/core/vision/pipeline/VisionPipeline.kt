package com.aura.core.vision.pipeline

import com.aura.core.vision.model.*
import com.aura.core.vision.provider.FrameProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface VisionPipeline {
    val state: StateFlow<PipelineState>
    val results: SharedFlow<PipelineResult>
    val events: SharedFlow<PipelineEvent>
    fun start(scope: CoroutineScope)
    fun stop()
}

@Singleton
class VisionPipelineImpl @Inject constructor(
    private val frameProvider: FrameProvider,
    private val orchestrator: VisionOrchestrator
) : VisionPipeline {

    private val _state = MutableStateFlow<PipelineState>(PipelineState.Idle)
    override val state: StateFlow<PipelineState> = _state.asStateFlow()

    private val _results = MutableSharedFlow<PipelineResult>(replay = 0, extraBufferCapacity = 16)
    override val results: SharedFlow<PipelineResult> = _results.asSharedFlow()

    private val _events = MutableSharedFlow<PipelineEvent>(replay = 0, extraBufferCapacity = 16)
    override val events: SharedFlow<PipelineEvent> = _events.asSharedFlow()

    private var processingJob: Job? = null
    private val mutex = Mutex()

    override fun start(scope: CoroutineScope) {
        scope.launch {
            mutex.withLock {
                if (processingJob != null) return@launch
                
                _state.value = PipelineState.ReceivingFrames
                _events.emit(PipelineEvent.Started)

                processingJob = scope.launch {
                    frameProvider.frames
                        .collect { frame ->
                            try {
                                _state.value = PipelineState.Processing
                                val result = orchestrator.process(frame)
                                _results.emit(result)
                                _events.emit(PipelineEvent.FrameProcessed(result))
                                _state.value = PipelineState.ReceivingFrames
                            } catch (e: Exception) {
                                _state.value = PipelineState.Error(e)
                                _events.emit(PipelineEvent.ErrorOccurred(e))
                            }
                        }
                }
            }
        }
    }

    override fun stop() {
        processingJob?.cancel()
        processingJob = null
        _state.value = PipelineState.Completed
        _state.value = PipelineState.Idle
        _events.tryEmit(PipelineEvent.Finished)
    }
}
