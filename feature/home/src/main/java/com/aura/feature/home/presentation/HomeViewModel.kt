package com.aura.feature.home.presentation

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val trending: List<OutfitModel>,
        val recommended: List<OutfitModel>,
        val savedOutfitIds: Set<String> = emptySet(),
        val categories: List<String> = listOf("All", "Summer Korean", "Oversized", "Business Casual", "Vintage")
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(
        val results: List<OutfitModel>,
        val savedOutfitIds: Set<String>
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val savedOutfitDao: SavedOutfitDao
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    var searchQuery = MutableStateFlow("")
        private set

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            try {
                // Collect saved outfit IDs to update UI state dynamically
                savedOutfitDao.getAllSavedOutfits().collect { savedList ->
                    val savedIds = savedList.map { it.id }.toSet()
                    
                    outfitRepository.getTrendingOutfits().collect { trends ->
                        outfitRepository.getRecommendedOutfits().collect { recs ->
                            _homeState.value = HomeUiState.Success(
                                trending = trends,
                                recommended = recs,
                                savedOutfitIds = savedIds
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _homeState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load home details.")
            }
        }
    }

    fun searchPins(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            _searchState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                savedOutfitDao.getAllSavedOutfits().collect { savedList ->
                    val savedIds = savedList.map { it.id }.toSet()
                    
                    outfitRepository.searchOutfits(query).collect { results ->
                        _searchState.value = SearchUiState.Success(
                            results = results,
                            savedOutfitIds = savedIds
                        )
                    }
                }
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.localizedMessage ?: "Search failed.")
            }
        }
    }

    fun toggleSaveOutfit(outfit: OutfitModel, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            if (isCurrentlySaved) {
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
