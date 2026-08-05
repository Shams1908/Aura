package com.aura.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class OutfitDto(
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

@Serializable
data class OutfitListResponse(
    val data: List<OutfitDto>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

