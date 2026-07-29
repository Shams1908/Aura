package com.aura.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PinterestPinDto(
    val id: String,
    val title: String?,
    val description: String?,
    val imageUrl: String,
    val sourceUrl: String?,
    val category: String? = null
)

@Serializable
data class PinSearchResponse(
    val data: List<PinterestPinDto>,
    val nextCursor: String?
)
