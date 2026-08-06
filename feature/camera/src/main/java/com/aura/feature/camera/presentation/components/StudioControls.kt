package com.aura.feature.camera.presentation.components

import android.net.Uri
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.core.designsystem.components.AuraButton
import com.aura.core.designsystem.components.AuraButtonType
import com.aura.core.designsystem.components.AuraChip

/**
 * Bottom control dashboard housing zoom indicators, Try-On action CTA, and shutter buttons.
 */
@Composable
fun StudioControls(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    onCaptureClick: () -> Unit,
    capturedImageUri: Uri?,
    onGalleryClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onTryOnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Custom SVG-like Programmatic Vector for Gallery fallback
    val galleryIcon = androidx.compose.runtime.remember {
        ImageVector.Builder(
            name = "GalleryIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.White),
                pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
            ) {
                moveTo(19f, 3f)
                lineTo(5f, 3f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                lineTo(3f, 19f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                lineTo(19f, 21f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                lineTo(21f, 5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                close()
                moveTo(19f, 19f)
                lineTo(5f, 19f)
                lineTo(5f, 5f)
                lineTo(19f, 5f)
                lineTo(19f, 19f)
                close()
                moveTo(14.14f, 11.86f)
                lineTo(11.14f, 15.86f)
                lineTo(9f, 13.14f)
                lineTo(6f, 17f)
                lineTo(18f, 17f)
                close()
            }
        }.build()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Zoom indicator chips (1.0x, 2.0x options)
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

        // 2. Large Interactive Brand Try-On CTA Button
        AuraButton(
            text = "AI Try-On",
            onClick = onTryOnClick,
            type = AuraButtonType.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(
                    BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Capture Shutter controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceSpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Thumbnail Button on Left
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onGalleryClick() },
                contentAlignment = Alignment.Center
            ) {
                if (capturedImageUri != null) {
                    AsyncImage(
                        model = capturedImageUri,
                        contentDescription = "Gallery Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = galleryIcon,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                }
            }

            // Central Shutter Button (Capture)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(BorderStroke(4.dp, Color.White), CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onCaptureClick() }
            )

            // Switch Camera Lens Button on Right
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
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
 * Arrangement helper to create custom spacing.
 */
private val Arrangement.SpaceSpaceBetween: Arrangement.Horizontal
    get() = Arrangement.SpaceBetween
