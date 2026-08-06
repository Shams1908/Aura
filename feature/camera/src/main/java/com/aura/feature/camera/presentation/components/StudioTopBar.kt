package com.aura.feature.camera.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Top action bar overlay for Aura Studio.
 */
@Composable
fun StudioTopBar(
    isFlashEnabled: Boolean,
    onFlashToggle: (Boolean) -> Unit,
    onCloseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Premium Lightning Bolt Flash vector
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close Button
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Studio",
                tint = Color.White
            )
        }

        // Action controls (Flash & Settings)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Flash Toggle
            IconButton(
                onClick = { onFlashToggle(!isFlashEnabled) },
                modifier = Modifier.background(
                    if (isFlashEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = flashIcon,
                    contentDescription = "Toggle Flash",
                    tint = if (isFlashEnabled) Color.Black else Color.White
                )
            }

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Studio Settings",
                    tint = Color.White
                )
            }
        }
    }
}
