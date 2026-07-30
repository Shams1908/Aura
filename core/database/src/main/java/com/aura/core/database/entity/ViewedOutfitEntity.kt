package com.aura.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "viewed_outfits")
data class ViewedOutfitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val brand: String,
    val imageUrl: String,
    val viewedAt: Long
)
