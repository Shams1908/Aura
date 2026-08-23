package com.aura.core.common.data

import kotlinx.serialization.Serializable

/**
 * Declares the explicit origin source of a try-on reference image.
 */
@Serializable
enum class ReferenceImageSource {
    DEFAULT_GALLERY,
    USER_DEVICE_GALLERY,
    USER_FILE_PICKER
}

/**
 * Unified metadata description for a try-on reference image.
 */
@Serializable
data class ReferenceImageMetadata(
    val title: String,
    val brand: String? = null,
    val category: String? = null,
    val sizeBytes: Long? = null,
    val mimeType: String? = null,
    val addedTimeMs: Long = System.currentTimeMillis()
)

/**
 * Single source of truth data contract representing the target try-on garment reference image,
 * regardless of whether it came from curated defaults, local device gallery, or a file picker.
 */
@Serializable
data class ReferenceImage(
    val uri: String,
    val source: ReferenceImageSource,
    val metadata: ReferenceImageMetadata? = null
)
