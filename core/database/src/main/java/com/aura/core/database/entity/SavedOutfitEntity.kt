package com.aura.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_outfits")
data class SavedOutfitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val sourceUrl: String?,
    val savedAt: Long
)
