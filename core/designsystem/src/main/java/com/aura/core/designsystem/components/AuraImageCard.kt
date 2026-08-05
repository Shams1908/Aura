package com.aura.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.aura.core.designsystem.theme.AuraElevation
import com.aura.core.designsystem.theme.AuraShapes

/**
 * Reusable image card wrapper utilizing Coil for image display.
 */
@Composable
fun AuraImageCard(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1.0f,
    onClick: (() -> Unit)? = null
) {
    val shape = AuraShapes.large
    val clickableModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    
    Card(
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = AuraElevation.none),
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
    ) {
        Box(modifier = Modifier.aspectRatio(aspectRatio)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
