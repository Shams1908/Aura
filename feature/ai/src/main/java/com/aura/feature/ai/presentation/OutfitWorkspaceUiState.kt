package com.aura.feature.ai.presentation

import com.aura.feature.ai.domain.model.DetectedClothing
import com.aura.feature.ai.domain.model.DetectedStyle

/**
 * UI State for the Outfit Workspace screen.
 */
sealed interface OutfitWorkspaceUiState {
    
    /**
     * Active state while loading core settings.
     */
    object Loading : OutfitWorkspaceUiState
    
    /**
     * Success state loaded with option details.
     */
    data class Success(
        val selectedImageUri: String? = null,
        val isProcessing: Boolean = false,
        val detectedItems: List<DetectedClothing> = emptyList(),
        val tips: List<String> = emptyList(),
        val detectedStyle: DetectedStyle? = null,
        val filename: String? = null,
        val dimensions: String? = null,
        val infoMessage: String? = null,
        val referenceImage: com.aura.core.common.data.ReferenceImage? = null
    ) : OutfitWorkspaceUiState
    
    /**
     * Error state representing operation failure.
     */
    data class Error(val message: String) : OutfitWorkspaceUiState
}
