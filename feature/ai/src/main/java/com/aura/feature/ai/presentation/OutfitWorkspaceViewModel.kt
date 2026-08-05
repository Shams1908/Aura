package com.aura.feature.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.ai.data.FakeOutfitWorkspaceRepository
import com.aura.feature.ai.domain.model.WorkspaceAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Outfit Workspace feature managing states and action triggers.
 */
@HiltViewModel
class OutfitWorkspaceViewModel @Inject constructor(
    private val repository: FakeOutfitWorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OutfitWorkspaceUiState>(OutfitWorkspaceUiState.Loading)
    val uiState: StateFlow<OutfitWorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadWorkspace()
    }

    /**
     * Initializes workspace settings.
     */
    fun loadWorkspace() {
        _uiState.value = OutfitWorkspaceUiState.Success(
            tips = repository.getTips()
        )
    }

    /**
     * Actions when selecting/uploading a mock image.
     */
    fun selectImage(uri: String) {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        
        val mockFilename = when {
            uri.contains("1556821840") -> "casual_hoodie_outfit.jpg"
            uri.contains("1515886657") -> "model_summer_wear.png"
            else -> "editorial_streetwear.jpg"
        }
        val mockDimensions = "1200 x 1600 px"

        _uiState.value = currentState.copy(
            selectedImageUri = uri,
            isProcessing = true,
            detectedItems = emptyList(),
            detectedStyle = null,
            filename = mockFilename,
            dimensions = mockDimensions,
            infoMessage = null
        )

        viewModelScope.launch {
            repository.detectClothing(uri).collect { result ->
                result.fold(
                    onSuccess = { items ->
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
                    },
                    onFailure = { error ->
                        _uiState.update {
                            OutfitWorkspaceUiState.Error(error.localizedMessage ?: "Failed to detect clothing")
                        }
                    }
                )
            }
        }
    }

    /**
     * Clears selected workspace picture.
     */
    fun removeImage() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        _uiState.value = currentState.copy(
            selectedImageUri = null,
            isProcessing = false,
            detectedItems = emptyList(),
            detectedStyle = null,
            filename = null,
            dimensions = null,
            infoMessage = null
        )
    }

    /**
     * Triggers simulated AI actions based on [WorkspaceAction] enums.
     */
    fun executeAction(action: WorkspaceAction) {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        val imageUri = currentState.selectedImageUri
        
        if (imageUri == null) {
            _uiState.update { state ->
                if (state is OutfitWorkspaceUiState.Success) {
                    state.copy(infoMessage = "Please upload an outfit image first.")
                } else {
                    state
                }
            }
            return
        }

        when (action) {
            WorkspaceAction.ANALYZE -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(isProcessing = true, infoMessage = null)
                    } else {
                        state
                    }
                }
                viewModelScope.launch {
                    repository.analyzeStyle(imageUri).collect { result ->
                        result.fold(
                            onSuccess = { style ->
                                _uiState.update { state ->
                                    if (state is OutfitWorkspaceUiState.Success) {
                                        state.copy(
                                            isProcessing = false,
                                            detectedStyle = style,
                                            infoMessage = "AI styling analysis completed successfully."
                                        )
                                    } else {
                                        state
                                    }
                                }
                            },
                            onFailure = { error ->
                                _uiState.update { state ->
                                    if (state is OutfitWorkspaceUiState.Success) {
                                        state.copy(
                                            isProcessing = false,
                                            infoMessage = "Error analyzing style: ${error.localizedMessage}"
                                        )
                                    } else {
                                        state
                                    }
                                }
                            }
                        )
                    }
                }
            }
            WorkspaceAction.TRY_ON -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(infoMessage = "Virtual Try-On simulation started. Preparing avatars.")
                    } else {
                        state
                    }
                }
            }
            WorkspaceAction.FIND_SIMILAR -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(infoMessage = "Searching visual catalogs for matches.")
                    } else {
                        state
                    }
                }
            }
            WorkspaceAction.SAVE -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(infoMessage = "Outfit workspace layout configuration saved.")
                    } else {
                        state
                    }
                }
            }
        }
    }

    /**
     * Dismisses active notification dialog message.
     */
    fun dismissInfo() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        _uiState.value = currentState.copy(infoMessage = null)
    }
}
