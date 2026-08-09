package com.aura.feature.ai.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.ai.domain.AnalyzeOutfitUseCase
import com.aura.feature.ai.inference.AIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val analyzeOutfitUseCase: AnalyzeOutfitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiDetectionState>(AiDetectionState.Idle)
    val uiState: StateFlow<AiDetectionState> = _uiState.asStateFlow()

    fun analyze(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = AiDetectionState.Loading
            val startTime = System.currentTimeMillis()
            try {
                val results = analyzeOutfitUseCase(bitmap)
                val duration = System.currentTimeMillis() - startTime
                _uiState.value = AiDetectionState.Success(results, duration)
            } catch (e: AIError) {
                _uiState.value = AiDetectionState.Error(e)
            } catch (e: Exception) {
                _uiState.value = AiDetectionState.Error(AIError.InferenceFailure(e))
            }
        }
    }

    fun reset() {
        _uiState.value = AiDetectionState.Idle
    }
}
