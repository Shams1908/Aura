package com.aura.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.profile.domain.repository.UserPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel coordinating logic and repository bindings for the User Photo Manager feature.
 */
@HiltViewModel
class UserPhotoViewModel @Inject constructor(
    private val repository: UserPhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserPhotoUiState>(UserPhotoUiState.Loading)
    val uiState: StateFlow<UserPhotoUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    /**
     * Subscribes to the list of user photos from repository.
     */
    fun loadPhotos() {
        _uiState.value = UserPhotoUiState.Loading
        viewModelScope.launch {
            repository.getPhotos().collect { result ->
                result.fold(
                    onSuccess = { photoList ->
                        _uiState.value = UserPhotoUiState.Success(photos = photoList)
                    },
                    onFailure = { error ->
                        _uiState.value = UserPhotoUiState.Error(
                            message = error.localizedMessage ?: "Failed to retrieve photos"
                        )
                    }
                )
            }
        }
    }

    /**
     * Uploads/registers a new photo URI.
     */
    fun uploadPhoto(uri: String, name: String) {
        val currentState = _uiState.value as? UserPhotoUiState.Success ?: return
        _uiState.value = currentState.copy(isUploading = true, message = null)
        
        viewModelScope.launch {
            repository.uploadPhoto(uri, name).collect { result ->
                result.fold(
                    onSuccess = { newPhoto ->
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(
                                    isUploading = false,
                                    message = "Photo '${newPhoto.name}' uploaded successfully."
                                )
                            } else {
                                state
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(
                                    isUploading = false,
                                    message = "Upload failed: ${error.localizedMessage}"
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

    /**
     * Selects default profile photo.
     */
    fun setDefaultPhoto(photoId: String) {
        val currentState = _uiState.value as? UserPhotoUiState.Success ?: return
        
        viewModelScope.launch {
            repository.setDefaultPhoto(photoId).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(message = "Default profile photo updated.")
                            } else {
                                state
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(message = "Failed to update default: ${error.localizedMessage}")
                            } else {
                                state
                            }
                        }
                    }
                )
            }
        }
    }

    /**
     * Renames an existing photo.
     */
    fun renamePhoto(photoId: String, newName: String) {
        val currentState = _uiState.value as? UserPhotoUiState.Success ?: return
        
        viewModelScope.launch {
            repository.renamePhoto(photoId, newName).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(message = "Photo renamed to '$newName'.")
                            } else {
                                state
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(message = "Rename failed: ${error.localizedMessage}")
                            } else {
                                state
                            }
                        }
                    }
                )
            }
        }
    }

    /**
     * Deletes a user photo.
     */
    fun deletePhoto(photoId: String) {
        val currentState = _uiState.value as? UserPhotoUiState.Success ?: return
        
        viewModelScope.launch {
            repository.deletePhoto(photoId).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(message = "Photo deleted.")
                            } else {
                                state
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            if (state is UserPhotoUiState.Success) {
                                state.copy(message = "Delete failed: ${error.localizedMessage}")
                            } else {
                                state
                            }
                        }
                    }
                )
            }
        }
    }

    /**
     * Clears overlay message tags.
     */
    fun clearMessage() {
        val currentState = _uiState.value as? UserPhotoUiState.Success ?: return
        _uiState.value = currentState.copy(message = null)
    }
}
