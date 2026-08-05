package com.aura.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aura.core.common.data.OutfitModel
import com.aura.core.common.data.OutfitRepository
import com.aura.core.common.network.NetworkMonitor
import com.aura.core.database.dao.SavedOutfitDao
import com.aura.core.database.entity.SavedOutfitEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val trending: List<OutfitModel>,
        val recommended: List<OutfitModel>,
        val recentTryOns: List<OutfitModel> = emptyList(),
        val savedOutfitIds: Set<String> = emptySet(),
        val categories: List<String> = listOf("All", "Summer Korean", "Oversized", "Business Casual", "Vintage")
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val savedOutfitDao: SavedOutfitDao,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val recentSearches: StateFlow<List<String>> = outfitRepository.getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchPagedResults: Flow<PagingData<OutfitModel>> = _searchQuery
        .flatMapLatest { query ->
            outfitRepository.searchOutfitsPaged(query)
        }
        .cachedIn(viewModelScope)

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            try {
                savedOutfitDao.getAllSavedOutfits().collect { savedList ->
                    val savedIds = savedList.map { it.id }.toSet()
                    
                    outfitRepository.getTrendingOutfits().collect { trends ->
                        outfitRepository.getRecommendedOutfits().collect { recs ->
                            outfitRepository.getRecentlyViewedOutfits().collect { recent ->
                                _homeState.value = HomeUiState.Success(
                                    trending = trends,
                                    recommended = recs,
                                    recentTryOns = recent,
                                    savedOutfitIds = savedIds
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _homeState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load home details.")
            }
        }
    }

    fun searchPins(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isNotBlank()) {
                outfitRepository.addSearchQueryToHistory(query)
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
