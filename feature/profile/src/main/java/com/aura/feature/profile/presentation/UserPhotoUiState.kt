package com.aura.feature.profile.presentation

import com.aura.feature.profile.domain.model.UserPhoto

/**
 * UI state representation for the User Photo Manager screen.
 */
sealed interface UserPhotoUiState {
    
    /**
     * Waiting state.
     */
    object Loading : UserPhotoUiState
    
    /**
     * Successful data state loaded with photo lists.
     */
    data class Success(
        val photos: List<UserPhoto> = emptyList(),
        val isUploading: Boolean = false,
        val message: String? = null
    ) : UserPhotoUiState
    
    /**
     * Error state representing operation failure.
     */
    data class Error(val message: String) : UserPhotoUiState
}
