package com.aura.feature.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.core.common.data.OutfitModel
import com.aura.core.designsystem.components.AuraCard
import com.aura.core.designsystem.components.AuraLoadingIndicator
import com.aura.core.designsystem.components.AuraSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    
    var showTryOnDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aura Studio", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = homeState) {
            is HomeUiState.Loading -> AuraLoadingIndicator()
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is HomeUiState.Success -> {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                ) {
                    // Search Bar Trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSearch() }
                    ) {
                        AuraSearchBar(
                            query = "",
                            onQueryChange = { onNavigateToSearch() },
                            placeholder = "Search Summer Korean, Oversized, vintage...",
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        )
                        // Layer a transparent box on top to block text input and force navigation click
                        Box(modifier = Modifier.matchParentSize().clickable { onNavigateToSearch() })
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Categories Selector
                    Text("Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.categories) { category ->
                            FilterChip(
                                selected = category == "All",
                                onClick = { onNavigateToSearch() },
                                label = { Text(category) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Trending Outfits Section
                    Text("Trending Outfits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(state.trending) { outfit ->
                            AuraCard(
                                title = outfit.title,
                                brand = outfit.brand,
                                imageUrl = outfit.imageUrl,
                                isSaved = state.savedOutfitIds.contains(outfit.id),
                                onSaveToggle = {
                                    viewModel.toggleSaveOutfit(outfit, state.savedOutfitIds.contains(outfit.id))
                                },
                                onClick = { onNavigateToDetail(outfit.id) },
                                onTryOnClick = { showTryOnDialog = true },
                                modifier = Modifier.width(180.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recommended outfits list
                    Text("Recommended For You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple grid for Recommended elements
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val chunks = state.recommended.chunked(2)
                        chunks.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { outfit ->
                                    AuraCard(
                                        title = outfit.title,
                                        brand = outfit.brand,
                                        imageUrl = outfit.imageUrl,
                                        isSaved = state.savedOutfitIds.contains(outfit.id),
                                        onSaveToggle = {
                                            viewModel.toggleSaveOutfit(outfit, state.savedOutfitIds.contains(outfit.id))
                                        },
                                        onClick = { onNavigateToDetail(outfit.id) },
                                        onTryOnClick = { showTryOnDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
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
