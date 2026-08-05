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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraTopBar
import com.aura.feature.camera.domain.CameraController
import com.aura.feature.camera.presentation.components.CameraBottomControls
import com.aura.feature.camera.presentation.components.CameraOverlay
import com.aura.feature.camera.presentation.components.CameraPreview
import com.aura.feature.camera.presentation.components.CameraStatusIndicator
import com.aura.feature.camera.presentation.components.CameraTopBar

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
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
        viewModel.onEvent(CameraEvent.PermissionResult(isGranted))
        if (isGranted) {
            controller.initialize(
                onInitialized = {
                    previewViewReference?.let {
                        controller.bindUseCase(
                            lifecycleOwner = lifecycleOwner,
                            previewView = it,
                            onPhotoCaptured = { uri -> viewModel.onEvent(CameraEvent.PhotoCaptured(uri)) },
                            onError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                        )
                    }
                },
                onError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Init failed")) }
            )
        }
    }

    // Check permission on entry
    LaunchedEffect(key1 = true) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val isGranted = permissionCheck == PackageManager.PERMISSION_GRANTED
        viewModel.onEvent(CameraEvent.PermissionResult(isGranted))

        if (isGranted) {
            controller.initialize(
                onInitialized = {
                    previewViewReference?.let {
                        controller.bindUseCase(
                            lifecycleOwner = lifecycleOwner,
                            previewView = it,
                            onPhotoCaptured = { uri -> viewModel.onEvent(CameraEvent.PhotoCaptured(uri)) },
                            onError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                        )
                    }
                },
                onError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Init failed")) }
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

    // Toggle flash and switch lens facing values on controller based on state changes
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
                // Permission Denied View
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
                // Viewfinder Immersive Container
                CameraPreview(
                    controller = controller,
                    onPreviewViewCreated = { previewView ->
                        previewViewReference = previewView
                        if (uiState.hasPermission) {
                            controller.bindUseCase(
                                lifecycleOwner = lifecycleOwner,
                                previewView = previewView,
                                onPhotoCaptured = { uri -> viewModel.onEvent(CameraEvent.PhotoCaptured(uri)) },
                                onError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                            )
                        }
                    }
                )

                // Mock Posture Guide Alignment Overlay
                CameraOverlay()

                // Top Actions Header
                CameraTopBar(
                    isFlashEnabled = uiState.isFlashEnabled,
                    onFlashToggle = { enabled -> viewModel.onEvent(CameraEvent.ToggleFlash(enabled)) },
                    onSwitchCamera = {
                        viewModel.onEvent(CameraEvent.SwitchCamera)
                        previewViewReference?.let {
                            controller.switchCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewView = it,
                                onPhotoCaptured = { uri -> viewModel.onEvent(CameraEvent.PhotoCaptured(uri)) },
                                onError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                            )
                        }
                    },
                    onCloseClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Top Middle Status Indicator
                CameraStatusIndicator(
                    status = uiState.status,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                )

                // Bottom Controls Shutter Panel
                CameraBottomControls(
                    currentZoom = uiState.zoomRatio,
                    onZoomChange = { ratio -> viewModel.onEvent(CameraEvent.SetZoom(ratio)) },
                    onCaptureClick = {
                        viewModel.onEvent(CameraEvent.CapturePhoto)
                        controller.capturePhoto(
                            onPhotoSaved = { uri -> viewModel.onEvent(CameraEvent.PhotoCaptured(uri)) },
                            onCaptureError = { ex -> viewModel.onEvent(CameraEvent.CaptureError(ex.localizedMessage ?: "Capture failed")) }
                        )
                    },
                    capturedImageUri = uiState.capturedImageUri,
                    onPreviewClick = {
                        // Triggers preview overlay visibility
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Captured Image Review Overlay (Slides up fullscreen above viewfinder)
            AnimatedVisibility(
                visible = uiState.status == CameraStatus.PREVIEW_MODE && uiState.capturedImageUri != null,
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

                        // Preview header top bar
                        AuraTopBar(
                            title = "Review Photo",
                            onNavigationClick = { viewModel.onEvent(CameraEvent.ResetPreview) }
                        )

                        // Bottom actions CTA (Use / Retake)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AuraButton(
                                text = "Use Photo",
                                type = AuraButtonType.Primary,
                                onClick = { onPhotoSelected(uiState.capturedImageUri!!) }
                            )

                            AuraButton(
                                text = "Retake",
                                type = AuraButtonType.Outlined,
                                onClick = { viewModel.onEvent(CameraEvent.ResetPreview) }
                            )
                        }
                    }
                }
            }

            // Error popup toast
            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
