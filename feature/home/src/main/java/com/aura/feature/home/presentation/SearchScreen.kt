package com.aura.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
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
    val query by viewModel.searchQuery.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val lazyPagingItems = viewModel.searchPagedResults.collectAsLazyPagingItems()
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
            // Offline State Banner
            if (!isOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You are currently offline. Displaying cached results.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            AuraSearchBar(
                query = query,
                onQueryChange = { viewModel.searchPins(it) },
                placeholder = "Search brands, styles, categories..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (query.isBlank()) {
                // Show Suggested & Recent Searches
                if (recentSearches.isNotEmpty()) {
                    Text("Recent Searches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { search ->
                            SuggestionChip(
                                onClick = { viewModel.searchPins(search) },
                                label = { Text(search) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text("Suggested Searches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            } else {
                // Show Paged Search Results
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(lazyPagingItems.itemCount) { index ->
                            val outfit = lazyPagingItems[index]
                            if (outfit != null) {
                                AuraCard(
                                    title = outfit.title,
                                    brand = outfit.brand,
                                    imageUrl = outfit.imageUrl,
                                    isSaved = false, // Keep UI clean
                                    onSaveToggle = {
                                        viewModel.toggleSaveOutfit(outfit, false)
                                    },
                                    onClick = { onNavigateToDetail(outfit.id) },
                                    onTryOnClick = { showTryOnDialog = true }
                                )
                            }
                        }

                        // Append Loading State handler inside grid
                        if (lazyPagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AuraLoadingIndicator(modifier = Modifier.size(40.dp))
                                }
                            }
                        }
                    }

                    // Main loading handler
                    when (val refreshState = lazyPagingItems.loadState.refresh) {
                        is LoadState.Loading -> {
                            AuraLoadingIndicator()
                        }
                        is LoadState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Error: ${refreshState.error.localizedMessage}",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { lazyPagingItems.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                        else -> {
                            if (lazyPagingItems.itemCount == 0) {
                                AuraEmptyState(
                                    title = "No Outfits Found",
                                    description = "We couldn't find any matches for \"$query\". Try search alternatives."
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
