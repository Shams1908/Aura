package com.aura.feature.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.ai.data.FakeOutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutfitWorkspaceViewModel @Inject constructor(
    private val fakeRepository: FakeOutfitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OutfitWorkspaceUiState>(OutfitWorkspaceUiState.Loading)
    val uiState: StateFlow<OutfitWorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadWorkspace()
    }

    fun loadWorkspace() {
        _uiState.value = OutfitWorkspaceUiState.Success(
            tips = fakeRepository.getTips()
        )
    }

    fun selectImage(uri: String) {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        
        _uiState.value = currentState.copy(
            selectedImageUri = uri,
            isProcessing = true,
            detectedItems = emptyList(),
            analysisResult = null
        )

        viewModelScope.launch {
            fakeRepository.detectGarments(uri).collect { items ->
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(
                            isProcessing = false,
                            detectedItems = items
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun removeImage() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        _uiState.value = currentState.copy(
            selectedImageUri = null,
            isProcessing = false,
            detectedItems = emptyList(),
            analysisResult = null
        )
    }

    fun runOutfitAnalysis() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        if (currentState.selectedImageUri == null) return
        
        _uiState.value = currentState.copy(isProcessing = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _uiState.update { state ->
                if (state is OutfitWorkspaceUiState.Success) {
                    state.copy(
                        isProcessing = false,
                        analysisResult = "AI INSIGHTS: This outfit is a highly harmonious blend of streetwear elements (Oversized Hoodie) and casual wear (Straight Fit Jeans). The color alignment is solid. Consider adding a vibrant sneaker overlay for accent pop."
                    )
                } else {
                    state
                }
            }
        }
    }

    fun clearAnalysis() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        _uiState.value = currentState.copy(analysisResult = null)
    }
}
