package com.aura.core.common.data

import com.aura.core.network.model.OutfitDto

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
