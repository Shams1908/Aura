package com.aura.feature.home.presentation

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aura.feature.home.presentation.components.EmptyHome
import com.aura.feature.home.presentation.components.FeatureCard
import com.aura.feature.home.presentation.components.HomeHeader
import com.aura.feature.home.presentation.components.LoadingHome
import com.aura.feature.home.presentation.components.QuickActionCard
import com.aura.feature.home.presentation.components.RecentTryOnCard
import com.aura.feature.home.presentation.components.SearchBar
import com.aura.feature.home.presentation.components.SectionTitle
import com.aura.feature.home.presentation.components.StyleCard
import com.aura.feature.home.presentation.components.UploadOutfitCard
import com.aura.feature.home.presentation.components.UploadPhotoCard

private data class QuickActionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    
    var showTryOnDialog by remember { mutableStateOf(false) }
    var showFeatureDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogText by remember { mutableStateOf("") }

    val quickActions = remember {
        listOf(
            QuickActionItem(
                title = "AI Stylist",
                description = "Custom advice",
                icon = Icons.Default.Star,
                color = Color(0xFFF3E5F5)
            ),
            QuickActionItem(
                title = "Virtual Closet",
                description = "Your clothes",
                icon = Icons.Default.Favorite,
                color = Color(0xFFE8F5E9)
            ),
            QuickActionItem(
                title = "Body Analyzer",
                description = "Find best cuts",
                icon = Icons.Default.Person,
                color = Color(0xFFE3F2FD)
            )
        )
    }

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        when (val state = homeState) {
            is HomeUiState.Loading -> {
                LoadingHome(modifier = Modifier.fillMaxSize())
            }
            is HomeUiState.Error -> {
                EmptyHome(
                    onRetry = { viewModel.loadHomeData() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is HomeUiState.Success -> {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Greeting Header
                    HomeHeader(
                        username = "Fashionista",
                        profileImageUrl = null,
                        onProfileClick = {
                            dialogTitle = "Profile settings"
                            dialogText = "Profile management is coming soon in Aura Studio."
                            showFeatureDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Search trigger
                    SearchBar(
                        query = "",
                        onSearchClick = onNavigateToSearch
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Upload options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UploadOutfitCard(
                            onClick = {
                                dialogTitle = "Upload Outfit"
                                dialogText = "Upload outfit feature will parse and extract individual garments from any input photo."
                                showFeatureDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                        UploadPhotoCard(
                            onClick = {
                                dialogTitle = "Upload Photo"
                                dialogText = "Take a photo to construct a 3D digital model of yourself for virtual fittings."
                                showFeatureDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Feature Card
                    FeatureCard(
                        title = "Chic Summer Editorial",
                        subtitle = "Curated AI outfits for your weekend style",
                        imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600",
                        onClick = {
                            dialogTitle = "Editorial Trend"
                            dialogText = "This style recommendation is generated based on your aesthetic profile and real-time trends."
                            showFeatureDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 5. Quick Actions
                    SectionTitle(title = "AI Features")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(quickActions) { action ->
                            QuickActionCard(
                                title = action.title,
                                icon = action.icon,
                                description = action.description,
                                onClick = {
                                    dialogTitle = action.title
                                    dialogText = "The ${action.title} tool is being integrated with our generative AI service."
                                    showFeatureDialog = true
                                },
                                containerColor = action.color
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Trending Styles (Horizontal List)
                    SectionTitle(
                        title = "Trending Styles",
                        actionText = "See All",
                        onActionClick = onNavigateToSearch
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(state.trending) { outfit ->
                            StyleCard(
                                outfit = outfit,
                                isSaved = state.savedOutfitIds.contains(outfit.id),
                                onSaveToggle = {
                                    viewModel.toggleSaveOutfit(outfit, state.savedOutfitIds.contains(outfit.id))
                                },
                                onClick = { onNavigateToDetail(outfit.id) },
                                onTryOn = { showTryOnDialog = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 7. Recent Try-ons (Horizontal List)
                    if (state.recentTryOns.isNotEmpty()) {
                        SectionTitle(title = "Recent Try-ons")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(state.recentTryOns) { outfit ->
                                RecentTryOnCard(
                                    outfit = outfit,
                                    dateText = "tried on 2h ago",
                                    onClick = { onNavigateToDetail(outfit.id) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showTryOnDialog) {
        AlertDialog(
            onDismissRequest = { showTryOnDialog = false },
            title = { Text("AI Virtual Try-On") },
            text = { Text("Generating fitting simulation... Stay tuned for complete Phase 2 try-on details!") },
            confirmButton = {
                TextButton(onClick = { showTryOnDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    if (showFeatureDialog) {
        AlertDialog(
            onDismissRequest = { showFeatureDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            confirmButton = {
                TextButton(onClick = { showFeatureDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
