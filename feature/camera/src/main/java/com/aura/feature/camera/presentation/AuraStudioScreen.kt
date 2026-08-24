package com.aura.feature.camera.presentation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextAlign
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.aura.core.common.data.ReferenceImageSource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraTopBar
import com.aura.feature.camera.domain.CameraController
import com.aura.feature.camera.presentation.components.CameraPreview
import com.aura.feature.camera.presentation.components.StudioBottomSheet
import com.aura.feature.camera.presentation.components.StudioControls
import com.aura.feature.camera.presentation.components.StudioStatusCard
import com.aura.feature.camera.presentation.components.StudioTopBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.feature.camera.presentation.overlay.OverlayRenderer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp

/**
 * Main viewport container for the Aura Studio camera experience.
 */
@Composable
fun AuraStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier,
    onPhotoSelected: (Uri) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    val controller = remember { CameraController(context, viewModel.frameStreamManager) }
    val streamStats by viewModel.streamStats.collectAsState()
    var previewViewReference by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }

    // Launcher for requesting camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(StudioEvent.PermissionResult(isGranted))
        if (isGranted) {
            controller.initialize(
                onInitialized = {
                    previewViewReference?.let {
                        controller.bindUseCase(
                            lifecycleOwner = lifecycleOwner,
                            previewView = it,
                            onPhotoCaptured = { uri -> viewModel.onEvent(StudioEvent.PhotoCaptured(uri)) },
                            onError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                        )
                    }
                },
                onError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Init failed")) }
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(StudioEvent.SelectCustomImage(uri, ReferenceImageSource.USER_DEVICE_GALLERY))
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(StudioEvent.SelectCustomImage(uri, ReferenceImageSource.USER_FILE_PICKER))
        }
    }

    android.util.Log.d("AURA_DEBUG", "AuraStudioScreen composed. uiState.hasPermission = ${uiState.hasPermission}, uiState.status = ${uiState.status}")

    // Check permission on screen entry
    LaunchedEffect(key1 = true) {
        android.util.Log.d("AURA_DEBUG", "AuraStudioScreen LaunchedEffect(key1 = true) check permission executed")
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val isGranted = permissionCheck == PackageManager.PERMISSION_GRANTED
        viewModel.onEvent(StudioEvent.PermissionResult(isGranted))

        if (isGranted) {
            controller.initialize(
                onInitialized = {
                    previewViewReference?.let {
                        controller.bindUseCase(
                            lifecycleOwner = lifecycleOwner,
                            previewView = it,
                            onPhotoCaptured = { uri -> viewModel.onEvent(StudioEvent.PhotoCaptured(uri)) },
                            onError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                        )
                    }
                },
                onError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Init failed")) }
            )
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(key1 = true) {
        android.util.Log.d("AURA_DEBUG", "AuraStudioScreen Lifecycle: ENTERED composition")
        onDispose {
            android.util.Log.d("AURA_DEBUG", "AuraStudioScreen Lifecycle: LEFT composition")
            controller.release()
        }
    }

    // Sync state settings to CameraController
    LaunchedEffect(uiState.isFlashEnabled) {
        android.util.Log.d("AURA_DEBUG", "AuraStudioScreen LaunchedEffect(uiState.isFlashEnabled) executed: ${uiState.isFlashEnabled}")
        controller.toggleFlash(uiState.isFlashEnabled)
    }

    LaunchedEffect(uiState.zoomRatio) {
        android.util.Log.d("AURA_DEBUG", "AuraStudioScreen LaunchedEffect(uiState.zoomRatio) executed: ${uiState.zoomRatio}")
        controller.setZoom(uiState.zoomRatio)
    }

    Scaffold(
        modifier = modifier.background(Color.Black)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!uiState.hasPermission) {
                // Permission Denied UI
                AuraEmptyState(
                    title = "Camera Permission Required",
                    description = "Aura requires access to the camera to align your try-on digital posture guide overlays.",
                    icon = Icons.Default.Info,
                    actionText = "Grant Permission",
                    onActionClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F0F12))
                )
            } else {
                // 1. Fullscreen Viewfinder CameraPreview (isolated CameraX)
                CameraPreview(
                    controller = controller,
                    onPreviewViewCreated = { previewView ->
                        previewViewReference = previewView
                        if (uiState.hasPermission) {
                            controller.bindUseCase(
                                lifecycleOwner = lifecycleOwner,
                                previewView = previewView,
                                onPhotoCaptured = { uri -> viewModel.onEvent(StudioEvent.PhotoCaptured(uri)) },
                                onError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                            )
                        }
                    }
                )

                // 2. Animated Center Alignment Guide Overlay (with debug toggle support)
                OverlayRenderer(viewModel = hiltViewModel(), isDebugMode = uiState.isDebugMode)

                // 3. Floating UI elements (Top layout elements stack)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StudioTopBar(
                        isFlashEnabled = uiState.isFlashEnabled,
                        onFlashToggle = { enabled -> viewModel.onEvent(StudioEvent.ToggleFlash(enabled)) },
                        isDebugMode = uiState.isDebugMode,
                        onDebugToggle = { viewModel.onEvent(StudioEvent.ToggleDebugMode) },
                        onCloseClick = onNavigateBack,
                        onSettingsClick = {
                            Toast.makeText(context, "Calibrating Studio Settings...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Upper Status Card (Tap to cycle status mocks) - Rendered only in debug mode
                    if (uiState.isDebugMode) {
                        StudioStatusCard(
                            status = uiState.status,
                            onStatusClick = {
                                val nextStatus = when (uiState.status) {
                                    StudioStatus.CAMERA_READY -> StudioStatus.TRACKING_WAITING
                                    StudioStatus.TRACKING_WAITING -> StudioStatus.OUTFIT_LOADED
                                    StudioStatus.OUTFIT_LOADED -> StudioStatus.CAMERA_READY
                                }
                                viewModel.onEvent(StudioEvent.SetStatus(nextStatus))
                            }
                        )
                    }
                }

                // 4. Compact Bottom Control Area (Minimalist overlay)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Floating Active Garment Pill (Selected Garment Indicator)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.onEvent(StudioEvent.SetBottomSheetExpanded(!uiState.isBottomSheetExpanded))
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = uiState.outfitThumbnailUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = "Reference: ${uiState.loadedOutfitName}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text(
                                    text = "• Change",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Floating Shutter / Action Controls Panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery Thumbnail on Left
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                    .clickable {
                                        if (uiState.capturedImageUri != null) {
                                            Toast.makeText(context, "Reviewing Captured Photo...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Loading Gallery...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.capturedImageUri != null) {
                                    AsyncImage(
                                        model = uiState.capturedImageUri,
                                        contentDescription = "Gallery Thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Info, // gallery fallback
                                        contentDescription = "Gallery",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Large Central Shutter Button (Capture)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(BorderStroke(4.dp, Color.White), CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        controller.capturePhoto(
                                            onPhotoSaved = { uri -> viewModel.onEvent(StudioEvent.PhotoCaptured(uri)) },
                                            onCaptureError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                                        )
                                    }
                            )

                            // Switch Camera Lens Button on Right
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(StudioEvent.SwitchCamera)
                                    previewViewReference?.let {
                                        controller.switchCamera(
                                            lifecycleOwner = lifecycleOwner,
                                            previewView = it,
                                            onPhotoCaptured = { uri -> viewModel.onEvent(StudioEvent.PhotoCaptured(uri)) },
                                            onError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Expanded Details Bottom Sheet Overlay
                AnimatedVisibility(
                    visible = uiState.isBottomSheetExpanded,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    StudioBottomSheet(
                        uiState = uiState,
                        onExpandedToggle = { expanded -> viewModel.onEvent(StudioEvent.SetBottomSheetExpanded(expanded)) },
                        onPickPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onPickFile = {
                            filePickerLauncher.launch("image/*")
                        },
                        onSelectOutfit = { outfit ->
                            viewModel.onEvent(StudioEvent.SelectDefaultOutfit(outfit.imageUrl, outfit))
                        }
                    )
                }

                // Render stream performance overlay conditionally in debug mode
                if (uiState.isDebugMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp, top = 160.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "STREAM PERFORMANCE",
                                color = Color(0xFF00E5FF),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Camera FPS: ${"%.1f".format(streamStats.inputFps)}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Process FPS: ${"%.1f".format(streamStats.processingFps)}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Dropped: ${streamStats.droppedFramesCount}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Latency: ${streamStats.pipelineLatencyMs} ms",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "State: ${streamStats.processingState}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // 5. Fullscreen Captured Outfit Image Review Overlay
            AnimatedVisibility(
                visible = uiState.capturedImageUri != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.capturedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = uiState.capturedImageUri,
                            contentDescription = "Captured image review",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Bar Header
                        AuraTopBar(
                            title = "Calibrate Photo",
                            onNavigationClick = { viewModel.onEvent(StudioEvent.ResetPreview) }
                        )

                        // Bottom Actions (Use Photo or Retake)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AuraButton(
                                text = "Confirm Fit Photo",
                                type = AuraButtonType.Primary,
                                onClick = {
                                    viewModel.confirmCalibrationPhoto(uiState.capturedImageUri!!)
                                    viewModel.startVirtualTryOn()
                                }
                            )

                            AuraButton(
                                text = "Retake",
                                type = AuraButtonType.Outlined,
                                onClick = { viewModel.onEvent(StudioEvent.ResetPreview) }
                            )
                        }
                    }
                }
            }

            // 6. Fullscreen Virtual Try-On Generation & Error Overlays
            when (val vtoState = uiState.vtoState) {
                is VirtualTryOnState.Generating -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F0F12))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aura AI generating your virtual try-on...",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aligning posture and rendering 3D garments.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                is VirtualTryOnState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F0F12))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Virtual Try-On Error",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = vtoState.errorMsg,
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AuraButton(
                                text = "Back to Workspace",
                                type = AuraButtonType.Primary,
                                onClick = {
                                    viewModel.resetVto()
                                    onNavigateBack()
                                }
                            )
                        }
                    }
                }
                else -> {
                    // Do nothing
                }
            }

            // Error display popup toast
            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
