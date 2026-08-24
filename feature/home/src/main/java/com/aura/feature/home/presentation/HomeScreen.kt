package com.aura.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.widget.Toast
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.feature.home.presentation.components.EmptyHome
import com.aura.feature.home.presentation.components.FeatureCard
import com.aura.feature.home.presentation.components.HomeHeader
import com.aura.feature.home.presentation.components.LoadingHome
import com.aura.feature.home.presentation.components.QuickActionCard
import com.aura.feature.home.presentation.components.RecentTryOnCard
import com.aura.feature.home.presentation.components.SearchBar
import com.aura.feature.home.presentation.components.SectionTitle
import com.aura.feature.home.presentation.components.StyleCard
import com.aura.feature.home.presentation.components.AuraStudioCard
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
    onNavigateToProfile: () -> Unit,
    onNavigateToWorkspace: (com.aura.core.common.data.ReferenceImage?) -> Unit,
    onNavigateToUserPhotos: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToTryOn: () -> Unit,
    onNavigateToSaved: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    var showAuraCollectionDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val metadata = validateAndGetMetadata(context, uri)
            if (metadata != null) {
                val refImage = com.aura.core.common.data.ReferenceImage(
                    uri = uri.toString(),
                    source = com.aura.core.common.data.ReferenceImageSource.USER_DEVICE_GALLERY,
                    metadata = metadata
                )
                onNavigateToWorkspace(refImage)
            } else {
                Toast.makeText(context, "Failed to load or validate the selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val metadata = validateAndGetMetadata(context, uri)
            if (metadata != null) {
                val refImage = com.aura.core.common.data.ReferenceImage(
                    uri = uri.toString(),
                    source = com.aura.core.common.data.ReferenceImageSource.USER_FILE_PICKER,
                    metadata = metadata
                )
                onNavigateToWorkspace(refImage)
            } else {
                Toast.makeText(context, "Failed to load or validate the selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    // Choose Source Dialog
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Select Outfit Source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how you'd like to import your reference outfit:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AuraButton(
                        text = "Device Photos",
                        type = AuraButtonType.Primary,
                        onClick = {
                            showSourceDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AuraButton(
                        text = "Browse Files",
                        type = AuraButtonType.Primary,
                        onClick = {
                            showSourceDialog = false
                            filePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AuraButton(
                        text = "Aura Collection",
                        type = AuraButtonType.Primary,
                        onClick = {
                            showSourceDialog = false
                            showAuraCollectionDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Aura Collection Selection Dialog
    if (showAuraCollectionDialog && homeState is HomeUiState.Success) {
        val successState = homeState as HomeUiState.Success
        AlertDialog(
            onDismissRequest = { showAuraCollectionDialog = false },
            title = { Text("Select Curated Outfit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Choose an outfit from our curated gallery:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(successState.trending.size) { index ->
                            val outfit = successState.trending[index]
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .clickable {
                                        showAuraCollectionDialog = false
                                        val refImage = com.aura.core.common.data.ReferenceImage(
                                            uri = outfit.imageUrl,
                                            source = com.aura.core.common.data.ReferenceImageSource.DEFAULT_GALLERY,
                                            metadata = com.aura.core.common.data.ReferenceImageMetadata(
                                                title = outfit.title,
                                                brand = outfit.brand,
                                                category = outfit.category
                                            )
                                        )
                                        onNavigateToWorkspace(refImage)
                                    }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    coil.compose.AsyncImage(
                                        model = outfit.imageUrl,
                                        contentDescription = outfit.title,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = outfit.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuraCollectionDialog = false }) {
                    Text("Cancel")
                }
            }
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
                        onProfileClick = onNavigateToProfile
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
                            onClick = { showSourceDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        AuraStudioCard(
                            onClick = onNavigateToTryOn,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Feature Card (Featured Banner with sample data)
                    FeatureCard(
                        title = "Chic Summer Editorial",
                        subtitle = "Curated AI outfits for your weekend style",
                        imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600",
                        onClick = {
                            val refImage = com.aura.core.common.data.ReferenceImage(
                                uri = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600",
                                source = com.aura.core.common.data.ReferenceImageSource.DEFAULT_GALLERY,
                                metadata = com.aura.core.common.data.ReferenceImageMetadata(
                                    title = "Chic Summer Editorial",
                                    brand = "Curated",
                                    category = "Edit"
                                )
                            )
                            onNavigateToWorkspace(refImage)
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
                                    when (action.title) {
                                        "AI Stylist" -> onNavigateToAnalysis()
                                        "Virtual Closet" -> onNavigateToSaved()
                                        "Body Analyzer" -> onNavigateToTryOn()
                                    }
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
                                onTryOn = onNavigateToTryOn
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
}

private fun validateAndGetMetadata(context: android.content.Context, uri: Uri): com.aura.core.common.data.ReferenceImageMetadata? {
    val contentResolver = context.contentResolver
    var pfd: android.os.ParcelFileDescriptor? = null
    try {
        pfd = contentResolver.openFileDescriptor(uri, "r")
        if (pfd == null) return null

        val mimeType = contentResolver.getType(uri) ?: "image/*"
        if (!mimeType.startsWith("image/")) {
            return null
        }

        val size = pfd.statSize
        var displayName = "Custom Image"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    displayName = cursor.getString(nameIndex)
                }
            }
        }

        return com.aura.core.common.data.ReferenceImageMetadata(
            title = displayName,
            sizeBytes = size,
            mimeType = mimeType,
            addedTimeMs = System.currentTimeMillis()
        )
    } catch (e: Exception) {
        android.util.Log.e("AURA_DEBUG", "Failed to open or validate URI: $uri", e)
        return null
    } finally {
        try {
            pfd?.close()
        } catch (ignored: Exception) {}
    }
}
