package com.aura.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.network.api.PinterestApi
import com.aura.core.network.model.PinterestPinDto
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
        val trending: List<PinterestPinDto>,
        val searchResults: List<PinterestPinDto> = emptyList(),
        val searchQuery: String = ""
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pinterestApi: PinterestApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrendingOutfits()
    }

    fun loadTrendingOutfits() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val trends = pinterestApi.getTrendingPins()
                _uiState.value = HomeUiState.Success(trending = trends)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load trending items")
            }
        }
    }

    fun searchPins(query: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                _uiState.update { currentState.copy(searchQuery = query) }
                try {
                    val searchResponse = pinterestApi.searchPins(query, null)
                    _uiState.update { 
                        currentState.copy(
                            searchResults = searchResponse.data,
                            searchQuery = query
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Pinterest Search Error")
                }
            }
        }
    }
}
