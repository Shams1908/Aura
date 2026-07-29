package com.aura.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.database.dao.SavedOutfitDao
import com.aura.core.database.entity.SavedOutfitEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(
        val savedOutfits: List<SavedOutfitEntity>,
        val notificationsEnabled: Boolean = true,
        val appVersion: String = "1.0.0-beta"
    ) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val savedOutfitDao: SavedOutfitDao
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = savedOutfitDao.getAllSavedOutfits()
        .map { outfits ->
            ProfileUiState.Success(
                savedOutfits = outfits,
                notificationsEnabled = true
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState.Loading
        )
}
