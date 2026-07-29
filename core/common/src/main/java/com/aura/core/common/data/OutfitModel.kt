package com.aura.core.common.data

import kotlinx.serialization.Serializable

@Serializable
data class OutfitModel(
    val id: String,
    val title: String,
    val brand: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val color: String,
    val tags: List<String>,
    val price: Double
)
