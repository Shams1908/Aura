package com.aura.feature.profile.domain.model

/**
 * Assessment metrics of a user photo for AI readiness.
 */
data class PhotoQuality(
    val poseFrontFacing: Boolean,
    val lightingGood: Boolean,
    val backgroundClean: Boolean,
    val resolutionHigh: Boolean
)

/**
 * Data model representing a user uploaded avatar photo used by Try-On engine.
 */
data class UserPhoto(
    val id: String,
    val uri: String,
    val name: String,
    val dateAdded: String,
    val isDefault: Boolean,
    val quality: PhotoQuality
)
