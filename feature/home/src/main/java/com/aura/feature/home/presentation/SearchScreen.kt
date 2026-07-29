package com.aura.feature.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aura.core.designsystem.components.AuraCard
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraLoadingIndicator
import com.aura.core.designsystem.components.AuraSearchBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val searchState by viewModel.searchState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    var showTryOnDialog by remember { mutableStateOf(false) }

    val suggestedSearches = listOf(
        "Summer Korean", "Oversized hoodie", "Business casual", 
        "Vintage denim", "Linen shirt", "Cargo pants"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Outfits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            AuraSearchBar(
                query = query,
                onQueryChange = { viewModel.searchPins(it) },
                placeholder = "Search brands, styles, categories..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = searchState) {
                is SearchUiState.Idle -> {
                    // Show suggested searches when no search query has been typed yet
                    Text("Suggested Searches", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestedSearches.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { viewModel.searchPins(suggestion) },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
                is SearchUiState.Loading -> {
                    AuraLoadingIndicator()
                }
                is SearchUiState.Error -> {
                    Text("Search error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        AuraEmptyState(
                            title = "No Outfits Found",
                            description = "We couldn't find any matches for \"$query\". Try search alternatives."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results) { outfit ->
                                AuraCard(
                                    title = outfit.title,
                                    brand = outfit.brand,
                                    imageUrl = outfit.imageUrl,
                                    isSaved = state.savedOutfitIds.contains(outfit.id),
                                    onSaveToggle = {
                                        viewModel.toggleSaveOutfit(outfit, state.savedOutfitIds.contains(outfit.id))
                                    },
                                    onClick = { onNavigateToDetail(outfit.id) },
                                    onTryOnClick = { showTryOnDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTryOnDialog) {
        AlertDialog(
            onDismissRequest = { showTryOnDialog = false },
            title = { Text("Phase 2 Try On") },
            text = { Text("Live camera tracking and AI garment try-on will be available in Phase 2. Stay tuned!") },
            confirmButton = {
                TextButton(onClick = { showTryOnDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}
