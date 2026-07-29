package com.aura.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val title: String,
    val brand: String,
    val price: Double,
    val currency: String,
    val imageUrl: String,
    val productUrl: String,
    val storeName: String, // H&M, Zara, Myntra, etc.
    val similarityScore: Float,
    val availability: Boolean
)
