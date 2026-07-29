package com.aura.feature.detail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraCard
import com.aura.core.designsystem.components.AuraLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OutfitDetailScreen(
    outfitId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: OutfitDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showTryOnDialog by remember { mutableStateOf(false) }

    LaunchedEffect(outfitId) {
        viewModel.loadOutfitDetails(outfitId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outfit Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is OutfitDetailUiState.Loading -> AuraLoadingIndicator()
            is OutfitDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is OutfitDetailUiState.Success -> {
                val outfit = state.outfit
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    AsyncImage(
                        model = outfit.imageUrl,
                        contentDescription = outfit.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = outfit.brand.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = outfit.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleSaveOutfit(outfit, state.isSaved) }
                            ) {
                                Icon(
                                    imageVector = if (state.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Save outfit",
                                    tint = if (state.isSaved) Color.Red else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${outfit.price}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = outfit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Color Palette
                        Text("Color Palette", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.colorPalette.forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tags
                        Text("Style Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            outfit.tags.forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(tag) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Try On button
                        AuraButton(
                            text = "TRY ON OUTFIT",
                            onClick = { showTryOnDialog = true }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Similar Outfits
                        if (state.similarOutfits.isNotEmpty()) {
                            Text("Similar Outfits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(state.similarOutfits) { simOutfit ->
                                    AuraCard(
                                        title = simOutfit.title,
                                        brand = simOutfit.brand,
                                        imageUrl = simOutfit.imageUrl,
                                        isSaved = false, // Simple mock state for similarity row
                                        onSaveToggle = {},
                                        onClick = { onNavigateToDetail(simOutfit.id) },
                                        onTryOnClick = { showTryOnDialog = true },
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
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
