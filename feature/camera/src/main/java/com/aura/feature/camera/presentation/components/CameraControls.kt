package com.aura.feature.camera.presentation.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraChip
import com.aura.feature.camera.presentation.CameraStatus

/**
 * Top control bar with camera configuration settings.
 */
@Composable
fun CameraTopBar(
    isFlashEnabled: Boolean,
    onFlashToggle: (Boolean) -> Unit,
    onSwitchCamera: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Custom programmatic Vector for Lightning Bolt Flash
    val flashIcon = remember {
        ImageVector.Builder(
            name = "Lightning",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11f, 15f)
                lineTo(4f, 15f)
                lineTo(13f, 1f)
                lineTo(13f, 9f)
                lineTo(20f, 9f)
                lineTo(11f, 23f)
                close()
            }
        }.build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Camera",
                tint = Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = { onFlashToggle(!isFlashEnabled) },
                modifier = Modifier.background(
                    if (isFlashEnabled) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.4f),
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = flashIcon,
                    contentDescription = "Toggle Flash",
                    tint = if (isFlashEnabled) MaterialTheme.colorScheme.onPrimary else Color.White
                )
            }

            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Custom camera status pill display.
 */
@Composable
fun CameraStatusIndicator(
    status: CameraStatus,
    modifier: Modifier = Modifier
) {
    val text = when (status) {
        CameraStatus.INITIALIZING -> "Initializing Studio..."
        CameraStatus.READY -> "Studio Ready"
        CameraStatus.PERMISSION_REQUIRED -> "Camera Permission Required"
        CameraStatus.CAPTURING -> "Capturing Outfit..."
        CameraStatus.PREVIEW_MODE -> "Capture Preview"
    }

    val backgroundColor = when (status) {
        CameraStatus.READY -> Color(0xFF00E5FF).copy(alpha = 0.2f)
        CameraStatus.CAPTURING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> Color.Black.copy(alpha = 0.5f)
    }

    val textColor = when (status) {
        CameraStatus.READY -> Color(0xFF00E5FF)
        CameraStatus.CAPTURING -> MaterialTheme.colorScheme.primary
        else -> Color.White
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (status == CameraStatus.INITIALIZING || status == CameraStatus.CAPTURING) {
                CircularProgressIndicator(
                    color = textColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Bottom control dashboard housing zoom buttons and capture shutter actions.
 */
@Composable
fun CameraBottomControls(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    onCaptureClick: () -> Unit,
    capturedImageUri: Uri?,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Zoom Chips (1x and 2x Quick Zoom Options)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraChip(
                text = "1.0x",
                selected = currentZoom == 1.0f,
                onClick = { onZoomChange(1.0f) }
            )
            AuraChip(
                text = "2.0x",
                selected = currentZoom == 2.0f,
                onClick = { onZoomChange(2.0f) }
            )
        }

        // Shutter Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Captured thumbnail preview on Left
            Box(modifier = Modifier.size(56.dp)) {
                if (capturedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(2.dp, Color.White), CircleShape)
                            .clip(CircleShape)
                            .clickable { onPreviewClick() }
                    ) {
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Thumbnail preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Capture Shutter Button Center
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(BorderStroke(5.dp, Color.White), CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onCaptureClick() }
            )

            // Placeholder block on Right to balance layout
            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}
