package com.aura.feature.ai.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.feature.ai.model.Detection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDetectionScreen(
    viewModel: AiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    val detections = (uiState as? AiDetectionState.Success)?.detections ?: emptyList()
    val isLoading = uiState is AiDetectionState.Loading
    val error = (uiState as? AiDetectionState.Error)?.error?.msg
    val duration = (uiState as? AiDetectionState.Success)?.inferenceTimeMs ?: 0L

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                loadedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
            } catch (e: Exception) {
                // Error handled gracefully
            }
        }
    }

    LaunchedEffect(loadedBitmap) {
        loadedBitmap?.let {
            viewModel.analyze(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Clothing Detection",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        containerColor = Color(0xFF121212),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Display Banner
            error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE57373).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE57373), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Error: $it",
                        color = Color(0xFFE57373),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected outfit image",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Draw Bounding Boxes Overlay
                    val bitmap = loadedBitmap
                    if (bitmap != null && detections.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val bmpWidth = bitmap.width.toFloat()
                            val bmpHeight = bitmap.height.toFloat()

                            // Calculate scaling parameters depending on how image fits in viewport (fit-center)
                            val scale = minOf(canvasWidth / bmpWidth, canvasHeight / bmpHeight)
                            val offsetX = (canvasWidth - bmpWidth * scale) / 2f
                            val offsetY = (canvasHeight - bmpHeight * scale) / 2f

                            for (detection in detections) {
                                val box = detection.boundingBox
                                
                                val left = offsetX + box.left * scale
                                val top = offsetY + box.top * scale
                                val right = offsetX + box.right * scale
                                val bottom = offsetY + box.bottom * scale

                                // Draw neon cyan box
                                drawRect(
                                    color = Color(0xFF00E5FF),
                                    topLeft = Offset(left, top),
                                    size = Size(right - left, bottom - top),
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Draw label text tag
                                drawContext.canvas.nativeCanvas.apply {
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#00E5FF")
                                        textSize = 12.dp.toPx()
                                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                                    }
                                    val text = "${detection.label} (${"%.0f".format(detection.confidence * 100)}%)"
                                    drawText(
                                        text,
                                        left + 4.dp.toPx(),
                                        maxOf(top - 6.dp.toPx(), 16.dp.toPx()),
                                        paint
                                    )
                                }
                            }
                        }
                    } else if (bitmap != null && uiState is AiDetectionState.Success && detections.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No fashion items detected.",
                                color = Color.LightGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No Outfit Loaded",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap Select Image below to run AI vision prediction",
                            color = Color.DarkGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Loading Spinner overlay
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF))
                    }
                }
            }

            // Stats / Metrics HUD Panel
            if (selectedImageUri != null && uiState is AiDetectionState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "DETECTION METRICS",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        HorizontalDivider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Detections Found:", color = Color.Gray, fontSize = 12.sp)
                            Text(text = "${detections.size}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Inference Duration:", color = Color.Gray, fontSize = 12.sp)
                            Text(text = "$duration ms", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Developer / Debug Info Section
            var debugExpanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEVELOPER DEBUG INFO",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        TextButton(
                            onClick = { debugExpanded = !debugExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (debugExpanded) "HIDE" else "SHOW",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (debugExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(8.dp))
                        val debugItems = listOf(
                            "Model Name" to "Fashion YOLO Nano (Fashionpedia)",
                            "Input Resolution" to "640 x 640 (RGB Float32 NCHW)",
                            "Output Shape" to "[1, 50, 8400]",
                            "Inference Provider" to "ONNX Runtime (CPU/NNAPI)",
                            "Confidence Threshold" to "0.35",
                            "IoU Threshold" to "0.45",
                            "Number of Classes" to "46",
                            "Inference Time" to "${duration} ms",
                            "Detections" to "${detections.size}"
                        )
                        debugItems.forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = value,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Interaction button
            AuraButton(
                text = "Select Image from Gallery",
                type = AuraButtonType.Primary,
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
