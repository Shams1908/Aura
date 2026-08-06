package com.aura.feature.camera.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.aura.feature.camera.presentation.components.StudioOverlay
import com.aura.feature.camera.presentation.components.StudioStatusCard
import com.aura.feature.camera.presentation.components.StudioTopBar

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

    val controller = remember { CameraController(context) }
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

    // Check permission on screen entry
    LaunchedEffect(key1 = true) {
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
        onDispose {
            controller.release()
        }
    }

    // Sync state settings to CameraController
    LaunchedEffect(uiState.isFlashEnabled) {
        controller.toggleFlash(uiState.isFlashEnabled)
    }

    LaunchedEffect(uiState.zoomRatio) {
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

                // 2. Animated Center Alignment Guide Overlay
                StudioOverlay()

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
                        onCloseClick = onNavigateBack,
                        onSettingsClick = {
                            Toast.makeText(context, "Calibrating Studio Settings...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Upper Status Card (Tap to cycle status mocks)
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

                // 4. Bottom controls panel and bottom sheet stack
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Persistent Dynamic Bottom Sheet
                    StudioBottomSheet(
                        uiState = uiState,
                        onExpandedToggle = { expanded -> viewModel.onEvent(StudioEvent.SetBottomSheetExpanded(expanded)) }
                    )

                    // Shutter / Action Controls Panel
                    StudioControls(
                        currentZoom = uiState.zoomRatio,
                        onZoomChange = { ratio -> viewModel.onEvent(StudioEvent.SetZoom(ratio)) },
                        onCaptureClick = {
                            controller.capturePhoto(
                                onPhotoSaved = { uri -> viewModel.onEvent(StudioEvent.PhotoCaptured(uri)) },
                                onCaptureError = { ex -> viewModel.onEvent(StudioEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                            )
                        },
                        capturedImageUri = uiState.capturedImageUri,
                        onGalleryClick = {
                            if (uiState.capturedImageUri != null) {
                                Toast.makeText(context, "Reviewing Captured Photo...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Loading System Gallery...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSwitchCamera = {
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
                        onTryOnClick = {
                            Toast.makeText(context, "Calibrating fit map for Try-On simulation...", Toast.LENGTH_LONG).show()
                        }
                    )
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
                                onClick = { onPhotoSelected(uiState.capturedImageUri!!) }
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

            // Error display popup toast
            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
