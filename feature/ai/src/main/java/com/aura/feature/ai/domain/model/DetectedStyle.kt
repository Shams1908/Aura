package com.aura.feature.ai.domain.model

data class DetectedStyle(
    val styleName: String,
    val confidence: Float,
    val mainPalette: List<String>,
    val occasion: String
)
