package com.aura.feature.ai.domain

data class DetectedClothingItem(
    val id: String,
    val name: String,
    val confidence: Float,
    val category: String
)
