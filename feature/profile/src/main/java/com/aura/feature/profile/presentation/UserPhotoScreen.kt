package com.aura.feature.profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraBottomSheet
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.core.designsystem.components.AuraCard
import com.aura.core.designsystem.components.AuraCardVariant
import com.aura.core.designsystem.components.AuraChip
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraErrorCard
import com.aura.core.designsystem.components.AuraSectionTitle
import com.aura.core.designsystem.components.AuraShimmer
import com.aura.core.designsystem.components.AuraTopBar
import com.aura.core.designsystem.components.AuraUploadCard
import com.aura.feature.profile.domain.model.UserPhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPhotoScreen(
    viewModel: UserPhotoViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var photoIdToDelete by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf(false) }
    var photoIdToRename by remember { mutableStateOf("") }
    var newPhotoName by remember { mutableStateOf("") }

    var selectedPhotoForActions by remember { mutableStateOf<UserPhoto?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()

    // Activity launcher for image picking from local gallery
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "tryon_photo_${System.currentTimeMillis()}.jpg"
            viewModel.uploadPhoto(it.toString(), fileName)
        }
    }

    Scaffold(
        topBar = {
            AuraTopBar(
                title = "My Photos",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is UserPhotoUiState.Loading -> {
                LoadingUserPhoto(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is UserPhotoUiState.Error -> {
                AuraEmptyState(
                    title = "Failed to Load Photos",
                    description = state.message,
                    icon = Icons.Default.Info,
                    actionText = "Retry",
                    onActionClick = { viewModel.loadPhotos() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is UserPhotoUiState.Success -> {
                val scrollState = rememberScrollState()
                val defaultPhoto = state.photos.find { it.isDefault }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage photos for AI Try-On",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1: Primary Upload Card
                    AuraUploadCard(
                        selectedImageUri = null,
                        isProcessing = state.isUploading,
                        title = "Upload Photo",
                        description = "Front-facing, well-lit images provide better AI results. Supports JPG, PNG.",
                        onUploadClick = { pickImageLauncher.launch("image/*") },
                        onReplaceClick = {},
                        onRemoveClick = {}
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 5: AI Readiness Card (Current Default Photo status checklist)
                    AuraSectionTitle(title = "AI Try-On Status")
                    
                    AuraCard(
                        variant = AuraCardVariant.Filled,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (defaultPhoto != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = MaterialTheme.shapes.small
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = defaultPhoto.uri,
                                            contentDescription = "Default preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Active Profile Photo",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = defaultPhoto.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Try-On Quality Assessment",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                val quality = defaultPhoto.quality
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QualityCheckRow(label = "Pose", isMet = quality.poseFrontFacing, metText = "Front Facing", unmetText = "Not Front Facing")
                                    QualityCheckRow(label = "Lighting", isMet = quality.lightingGood, metText = "Good", unmetText = "Bad")
                                    QualityCheckRow(label = "Background", isMet = quality.backgroundClean, metText = "Clean", unmetText = "Cluttered")
                                    QualityCheckRow(label = "Resolution", isMet = quality.resolutionHigh, metText = "High", unmetText = "Low")
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "No default profile photo selected. Upload and select a photo to begin Try-On.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 2: Saved Photos
                    AuraSectionTitle(title = "Saved Photos")

                    if (state.photos.isEmpty()) {
                        AuraEmptyState(
                            title = "No Photos Yet",
                            description = "Your uploaded photos will be listed here.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        // Render a grid layout of photo cards
                        val chunkedPhotos = state.photos.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            chunkedPhotos.forEach { rowPhotos ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowPhotos.forEach { photo ->
                                        AuraCard(
                                            variant = AuraCardVariant.Elevated,
                                            onClick = {
                                                selectedPhotoForActions = photo
                                                showBottomSheet = true
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(1.0f)
                                                ) {
                                                    AsyncImage(
                                                        model = photo.uri,
                                                        contentDescription = photo.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    
                                                    if (photo.isDefault) {
                                                        Box(modifier = Modifier.padding(8.dp)) {
                                                            AuraChip(
                                                                text = "Default",
                                                                selected = true,
                                                                onClick = {}
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        text = photo.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Added: ${photo.dateAdded}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Empty cell if row has only 1 item
                                    if (rowPhotos.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Confirmation delete prompt
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Photo") },
            text = { Text("Are you sure you want to permanently delete this photo? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhoto(photoIdToDelete)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename dialog prompt
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Photo") },
            text = {
                Column {
                    Text("Enter a new file label for this photo:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhotoName,
                        onValueChange = { newPhotoName = it },
                        singleLine = true,
                        label = { Text("Filename") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPhotoName.isNotBlank()) {
                            viewModel.renamePhoto(photoIdToRename, newPhotoName)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Photo Actions Bottom Sheet
    if (showBottomSheet && selectedPhotoForActions != null) {
        val photo = selectedPhotoForActions!!
        
        AuraBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = photo.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Added on ${photo.dateAdded}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (!photo.isDefault) {
                    AuraButton(
                        text = "Set as Default",
                        type = AuraButtonType.Primary,
                        onClick = {
                            viewModel.setDefaultPhoto(photo.id)
                            showBottomSheet = false
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AuraButton(
                    text = "Rename Photo",
                    type = AuraButtonType.Secondary,
                    onClick = {
                        photoIdToRename = photo.id
                        newPhotoName = photo.name
                        showRenameDialog = true
                        showBottomSheet = false
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                AuraButton(
                    text = "Delete Photo",
                    type = AuraButtonType.Outlined,
                    onClick = {
                        photoIdToDelete = photo.id
                        showDeleteDialog = true
                        showBottomSheet = false
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Operation status notification banner
    val currentState = uiState as? UserPhotoUiState.Success
    if (currentState?.message != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("Photo Manager") },
            text = { Text(currentState.message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun QualityCheckRow(
    label: String,
    isMet: Boolean,
    metText: String,
    unmetText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isMet) metText else unmetText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isMet) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoadingUserPhoto(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuraShimmer(modifier = Modifier.size(180.dp, 28.dp))
        AuraShimmer(modifier = Modifier.size(240.dp, 16.dp))
        AuraShimmer(modifier = Modifier.fillMaxWidth().height(180.dp))
        AuraShimmer(modifier = Modifier.fillMaxWidth().height(120.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuraShimmer(modifier = Modifier.weight(1f).height(150.dp))
            AuraShimmer(modifier = Modifier.weight(1f).height(150.dp))
        }
    }
}
