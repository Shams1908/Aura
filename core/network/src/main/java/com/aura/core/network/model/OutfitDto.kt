package com.aura.core.network.model

import com.aura.core.common.data.OutfitModel
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

fun OutfitDto.toDomain(): OutfitModel {
    return OutfitModel(
        id = this.id,
        title = this.title,
        brand = this.brand,
        description = this.description,
        imageUrl = this.imageUrl,
        category = this.category,
        color = this.color,
        tags = this.tags,
        price = this.price
    )
}
