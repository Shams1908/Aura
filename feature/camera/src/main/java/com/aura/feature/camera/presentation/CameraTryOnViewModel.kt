package com.aura.feature.camera.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.ai.domain.GarmentParser
import com.aura.feature.ai.domain.GarmentType
import com.aura.feature.ai.domain.ParsedGarment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CameraTryOnUiState {
    object Initializing : CameraTryOnUiState
    data class ProcessingGarment(val message: String) : CameraTryOnUiState
    data class TryOnReady(
        val parsedGarment: ParsedGarment,
        val isHumanDetected: Boolean,
        val fps: Float,
        val statusMessage: String
    ) : CameraTryOnUiState
    data class Error(val errorMsg: String) : CameraTryOnUiState
}

@HiltViewModel
class CameraTryOnViewModel @Inject constructor(
    private val garmentParser: GarmentParser
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraTryOnUiState>(CameraTryOnUiState.Initializing)
    val uiState: StateFlow<CameraTryOnUiState> = _uiState.asStateFlow()

    fun initializeTryOn(pinImageUrl: String) {
        viewModelScope.launch {
            _uiState.update { CameraTryOnUiState.ProcessingGarment("Downloading and processing outfit...") }
            
            // Assume image is fetched locally first
            // val imageBitmap = networkImageLoader.load(pinImageUrl)
            // val parsed = garmentParser.parseGarment(imageBitmap, GarmentType.UPPER_BODY)
            
            // Simulating placeholder state until verification is fully completed
            _uiState.update { 
                CameraTryOnUiState.Error("Pending user model downloading/loading")
            }
        }
    }

    fun updateHumanDetection(detected: Boolean) {
        val current = _uiState.value
        if (current is CameraTryOnUiState.TryOnReady) {
            _uiState.update {
                current.copy(isHumanDetected = detected)
            }
        }
    }

    fun updateFps(fps: Float) {
        val current = _uiState.value
        if (current is CameraTryOnUiState.TryOnReady) {
            _uiState.update {
                current.copy(fps = fps)
            }
        }
    }
}
