package com.aura.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trending_outfits")
data class TrendingOutfitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val brand: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val color: String,
    val tags: String, // Stored as comma-separated values
    val price: Double,
    val cachedAt: Long
)
