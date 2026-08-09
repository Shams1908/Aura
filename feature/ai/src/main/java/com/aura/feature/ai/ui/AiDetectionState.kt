package com.aura.feature.ai.ui

import com.aura.feature.ai.inference.AIError
import com.aura.feature.ai.model.Detection

sealed interface AiDetectionState {
    data object Idle : AiDetectionState
    data object Loading : AiDetectionState
    data class Success(
        val detections: List<Detection>,
        val inferenceTimeMs: Long
    ) : AiDetectionState
    data class Error(
        val error: AIError
    ) : AiDetectionState
}
