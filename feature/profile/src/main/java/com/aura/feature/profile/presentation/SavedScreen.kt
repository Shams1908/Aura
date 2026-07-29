package com.aura.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.core.database.entity.SavedOutfitEntity
import com.aura.core.designsystem.components.AuraCard
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showTryOnDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Outfits", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> AuraLoadingIndicator()
            is ProfileUiState.Success -> {
                val savedList = state.savedOutfits
                if (savedList.isEmpty()) {
                    AuraEmptyState(
                        title = "No Saved Outfits",
                        description = "Tap the heart icon on any outfit card to save styles for offline reference."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = paddingValues,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(savedList) { entity ->
                            AuraCard(
                                title = entity.title,
                                brand = "SAVED STYLE",
                                imageUrl = entity.imageUrl,
                                isSaved = true,
                                onSaveToggle = {
                                    viewModel.unsaveOutfit(entity)
                                },
                                onClick = { onNavigateToDetail(entity.id) },
                                onTryOnClick = { showTryOnDialog = true }
                            )
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
