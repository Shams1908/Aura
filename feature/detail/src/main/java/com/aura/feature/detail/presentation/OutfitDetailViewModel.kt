package com.aura.feature.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.common.data.OutfitModel
import com.aura.core.common.data.OutfitRepository
import com.aura.core.database.dao.SavedOutfitDao
import com.aura.core.database.entity.SavedOutfitEntity
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
        val outfit: OutfitModel,
        val isSaved: Boolean,
        val colorPalette: List<String> = listOf("#1E1E24", "#7F7F7F", "#C0A98F", "#F9F9FB"),
        val similarOutfits: List<OutfitModel> = emptyList()
    ) : OutfitDetailUiState
    data class Error(val message: String) : OutfitDetailUiState
}

@HiltViewModel
class OutfitDetailViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val savedOutfitDao: SavedOutfitDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<OutfitDetailUiState>(OutfitDetailUiState.Loading)
    val uiState: StateFlow<OutfitDetailUiState> = _uiState.asStateFlow()

    fun loadOutfitDetails(outfitId: String) {
        viewModelScope.launch {
            _uiState.value = OutfitDetailUiState.Loading
            try {
                outfitRepository.getOutfitDetails(outfitId).collect { outfit ->
                    if (outfit != null) {
                        savedOutfitDao.isOutfitSaved(outfitId).collect { saved ->
                            outfitRepository.getRecommendedOutfits().collect { similar ->
                                _uiState.value = OutfitDetailUiState.Success(
                                    outfit = outfit,
                                    isSaved = saved,
                                    similarOutfits = similar.filter { it.id != outfitId }.take(5)
                                )
                            }
                        }
                    } else {
                        _uiState.value = OutfitDetailUiState.Error("Outfit not found.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = OutfitDetailUiState.Error(e.localizedMessage ?: "Failed to load details.")
            }
        }
    }

    fun toggleSaveOutfit(outfit: OutfitModel, currentlySaved: Boolean) {
        viewModelScope.launch {
            if (currentlySaved) {
                savedOutfitDao.deleteOutfit(
                    SavedOutfitEntity(
                        id = outfit.id,
                        title = outfit.title,
                        description = outfit.description,
                        imageUrl = outfit.imageUrl,
                        sourceUrl = null,
                        savedAt = System.currentTimeMillis()
                    )
                )
            } else {
                savedOutfitDao.saveOutfit(
                    SavedOutfitEntity(
                        id = outfit.id,
                        title = outfit.title,
                        description = outfit.description,
                        imageUrl = outfit.imageUrl,
                        sourceUrl = null,
                        savedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
