package com.aura.feature.camera.presentation.overlay

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class OverlayStyle(
    val primaryColor: Color = Color(0xFF00E5FF),
    val secondaryColor: Color = Color(0xFFFF007F),
    val strokeWidth: Dp = 2.dp,
    val alpha: Float = 1.0f,
    val textStyle: TextStyle? = null
)
