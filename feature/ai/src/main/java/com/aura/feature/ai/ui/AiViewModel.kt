package com.aura.feature.ai.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.ai.domain.AnalyzeOutfitUseCase
import com.aura.feature.ai.model.OutfitAnalysis
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

    private val _analysisState = MutableStateFlow<OutfitAnalysis?>(null)
    val analysisState: StateFlow<OutfitAnalysis?> = _analysisState.asStateFlow()

    fun analyze(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val result = analyzeOutfitUseCase(bitmap)
                _analysisState.value = result
            } catch (e: Exception) {
                // Error handling placeholder
            }
        }
    }
}
