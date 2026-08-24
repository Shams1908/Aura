package com.aura.feature.ai.presentation

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.core.designsystem.components.AuraCard
import com.aura.core.designsystem.components.AuraCardVariant
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraErrorCard
import com.aura.core.designsystem.components.AuraShimmer
import com.aura.core.designsystem.components.AuraSectionTitle
import com.aura.core.designsystem.components.AuraTopBar
import com.aura.core.designsystem.components.AuraUploadCard
import com.aura.feature.ai.domain.model.WorkspaceAction

private val mockOutfitImages = listOf(
    "https://images.unsplash.com/photo-1556821840-3a63f95609a7?q=80&w=600",
    "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600",
    "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?q=80&w=600"
)

@Composable
fun OutfitWorkspaceScreen(
    viewModel: OutfitWorkspaceViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToUserPhotos: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToTryOn: () -> Unit = {},
    onNavigateToSimilarProducts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var imageIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            AuraTopBar(
                title = "Outfit Workspace",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is OutfitWorkspaceUiState.Loading -> {
                LoadingOutfitWorkspace(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is OutfitWorkspaceUiState.Error -> {
                AuraEmptyState(
                    title = "Failed to Load Workspace",
                    description = state.message,
                    icon = Icons.Default.Info,
                    actionText = "Retry",
                    onActionClick = { viewModel.loadWorkspace() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is OutfitWorkspaceUiState.Success -> {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Upload an outfit to begin AI analysis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1: Selected Reference Card or Upload Card
                    if (state.selectedImageUri != null) {
                        AuraCard(
                            variant = AuraCardVariant.Filled,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                coil.compose.AsyncImage(
                                    model = state.selectedImageUri,
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SELECTED REFERENCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = state.filename ?: "Custom Reference Image",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (state.dimensions != null) {
                                        Text(
                                            text = state.dimensions,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AuraButton(
                                    text = "Remove",
                                    type = AuraButtonType.Secondary,
                                    onClick = { viewModel.removeImage() }
                                )
                            }
                        }
                    } else {
                        AuraUploadCard(
                            selectedImageUri = state.selectedImageUri,
                            isProcessing = state.isProcessing,
                            title = "Upload Outfit",
                            description = "Supports high-resolution JPG or PNG",
                            onUploadClick = {
                                val nextImage = mockOutfitImages[imageIndex]
                                imageIndex = (imageIndex + 1) % mockOutfitImages.size
                                viewModel.selectImage(nextImage)
                            },
                            onReplaceClick = {},
                            onRemoveClick = {}
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 2: Detected Clothing
                    if (state.selectedImageUri != null) {
                        AuraSectionTitle(title = "Detected Clothing")
                        
                        if (state.isProcessing && state.detectedItems.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AuraShimmer(modifier = Modifier.size(120.dp, 28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                AuraShimmer(modifier = Modifier.size(100.dp, 28.dp))
                            }
                        } else {
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.detectedItems.forEach { garment ->
                                    com.aura.core.designsystem.components.AuraChip(
                                        text = garment.name,
                                        selected = true,
                                        onClick = {}
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // SECTION 3: AI Workspace action cards
                    AuraSectionTitle(title = "AI Workspace")
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AuraCard(
                                variant = AuraCardVariant.Elevated,
                                onClick = onNavigateToAnalysis,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Analyze Outfit",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Understand colors, balance and style.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                            
                            AuraCard(
                                variant = AuraCardVariant.Elevated,
                                onClick = onNavigateToTryOn,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Virtual Try-On",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "See yourself wearing this outfit.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AuraCard(
                                variant = AuraCardVariant.Elevated,
                                onClick = onNavigateToSimilarProducts,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Find Similar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Search visually similar clothing.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                            
                            AuraCard(
                                variant = AuraCardVariant.Elevated,
                                onClick = { viewModel.executeAction(WorkspaceAction.SAVE) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Save Workspace",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Store this outfit for later.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 4: AI Preview
                    if (state.detectedStyle != null) {
                        AuraSectionTitle(title = "AI Preview")
                        AuraCard(
                            variant = AuraCardVariant.Gradient,
                            gradientColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI ANALYSIS STATUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Detected Style",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = state.detectedStyle.styleName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Confidence",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "${(state.detectedStyle.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Main Palette",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = state.detectedStyle.mainPalette.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Occasion",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = state.detectedStyle.occasion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // SECTION 5: Tips
                    if (state.tips.isNotEmpty()) {
                        AuraSectionTitle(title = "Tips")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.tips.forEach { tip ->
                                AuraInfoCard(tip = tip)
                            }
                        }
                    }
                    
                    if (state.selectedImageUri != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AuraButton(
                            text = "Continue to Photo Manager",
                            type = AuraButtonType.Primary,
                            onClick = onNavigateToUserPhotos
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Info Message overlay feedback
    val currentState = uiState as? OutfitWorkspaceUiState.Success
    if (currentState?.infoMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissInfo() },
            title = { Text("Workspace Notification") },
            text = { Text(currentState.infoMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissInfo() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AuraInfoCard(
    tip: String,
    modifier: Modifier = Modifier
) {
    AuraCard(
        variant = AuraCardVariant.Filled,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LoadingOutfitWorkspace(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuraShimmer(modifier = Modifier.size(180.dp, 28.dp))
        AuraShimmer(modifier = Modifier.size(260.dp, 16.dp))
        AuraShimmer(modifier = Modifier.fillMaxWidth().height(180.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuraShimmer(modifier = Modifier.weight(1f).height(120.dp))
            AuraShimmer(modifier = Modifier.weight(1f).height(120.dp))
        }
    }
}
