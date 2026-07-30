package com.aura.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val queryText: String,
    val searchedAt: Long
)
