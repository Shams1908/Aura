package com.aura.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Premium fashion theme palette (sleek, minimal, high contrast darks)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E1E24),      // Deep Charcoal
    secondary = Color(0xFF7F7F7F),    // Neutral Gray
    tertiary = Color(0xFFC0A98F),     // Light Bronze/Warm Sand
    background = Color(0xFFF9F9FB),   // Clean off-white
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1E24),
    onSurface = Color(0xFF1E1E24)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF9F9FB),      // Clean white on dark backgrounds
    secondary = Color(0xFF8F8F94),    // Premium silver
    tertiary = Color(0xFFE2D1C3),     // Bronze/Champagne accents
    background = Color(0xFF0F0F12),   // True deep space dark
    surface = Color(0xFF16161A),      // Slightly lighter card container background
    onPrimary = Color(0xFF0F0F12),
    onSecondary = Color(0xFF0F0F12),
    onBackground = Color(0xFFF9F9FB),
    onSurface = Color(0xFFF9F9FB)
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
