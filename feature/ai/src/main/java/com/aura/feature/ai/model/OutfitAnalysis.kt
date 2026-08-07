package com.aura.feature.ai.model

data class OutfitAnalysis(
    val items: List<ClothingItem>,
    val style: String,
    val caption: String
)
