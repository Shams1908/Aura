package com.aura.feature.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.database.dao.SavedOutfitDao
import com.aura.core.database.entity.SavedOutfitEntity
import com.aura.core.network.api.PinterestApi
import com.aura.core.network.model.PinterestPinDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OutfitDetailUiState {
    object Loading : OutfitDetailUiState
    data class Success(
        val pin: PinterestPinDto,
        val isSaved: Boolean,
        val colorPalette: List<String> = emptyList()
    ) : OutfitDetailUiState
    data class Error(val message: String) : OutfitDetailUiState
}

@HiltViewModel
class OutfitDetailViewModel @Inject constructor(
    private val pinterestApi: PinterestApi,
    private val savedOutfitDao: SavedOutfitDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<OutfitDetailUiState>(OutfitDetailUiState.Loading)
    val uiState: StateFlow<OutfitDetailUiState> = _uiState.asStateFlow()

    fun loadOutfitDetails(pinId: String) {
        viewModelScope.launch {
            _uiState.value = OutfitDetailUiState.Loading
            try {
                val pin = pinterestApi.getPinDetail(pinId)
                
                // Track save state live from DB
                savedOutfitDao.isOutfitSaved(pinId).collect { saved ->
                    // Sample extracted color palette
                    val palette = listOf("#1E1E24", "#7F7F7F", "#C0A98F", "#F9F9FB")
                    _uiState.value = OutfitDetailUiState.Success(
                        pin = pin,
                        isSaved = saved,
                        colorPalette = palette
                    )
                }
            } catch (e: Exception) {
                _uiState.value = OutfitDetailUiState.Error(e.localizedMessage ?: "Failed to load details")
            }
        }
    }

    fun toggleSaveOutfit(pin: PinterestPinDto, currentlySaved: Boolean) {
        viewModelScope.launch {
            if (currentlySaved) {
                savedOutfitDao.deleteOutfit(
                    SavedOutfitEntity(
                        id = pin.id,
                        title = pin.title ?: "Untitled Outfit",
                        description = pin.description,
                        imageUrl = pin.imageUrl,
                        sourceUrl = pin.sourceUrl,
                        savedAt = System.currentTimeMillis()
                    )
                )
            } else {
                savedOutfitDao.saveOutfit(
                    SavedOutfitEntity(
                        id = pin.id,
                        title = pin.title ?: "Untitled Outfit",
                        description = pin.description,
                        imageUrl = pin.imageUrl,
                        sourceUrl = pin.sourceUrl,
                        savedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
