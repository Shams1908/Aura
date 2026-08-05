package com.aura.feature.ai.presentation

import com.aura.feature.ai.domain.DetectedClothingItem

sealed interface OutfitWorkspaceUiState {
    object Loading : OutfitWorkspaceUiState
    
    data class Success(
        val selectedImageUri: String? = null,
        val isProcessing: Boolean = false,
        val detectedItems: List<DetectedClothingItem> = emptyList(),
        val tips: List<String> = emptyList(),
        val analysisResult: String? = null
    ) : OutfitWorkspaceUiState
    
    data class Error(val message: String) : OutfitWorkspaceUiState
}
