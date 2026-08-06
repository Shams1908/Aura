package com.aura.feature.camera.presentation

import android.net.Uri

/**
 * Mock status modes for prototyping Aura Studio states.
 */
enum class StudioStatus {
    CAMERA_READY,
    TRACKING_WAITING,
    OUTFIT_LOADED
}

/**
 * Screen state model for the Aura Studio camera interface.
 */
data class StudioUiState(
    val status: StudioStatus = StudioStatus.CAMERA_READY,
    val isFrontCamera: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val capturedImageUri: Uri? = null,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null,
    
    // Bottom Sheet state
    val isBottomSheetExpanded: Boolean = false,
    
    // Loaded Outfit mock metadata
    val loadedOutfitName: String = "Midnight Noir Velvet Gown",
    val outfitThumbnailUrl: String = "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500&auto=format&fit=crop&q=80",
    val detectedStyle: String = "Avanguardia Noir",
    val outfitDescription: String = "A masterpiece of structure and drape. This gown is crafted from heavy Italian velvet, contouring the upper torso and opening into a full floor-length skirt with a subtle slit. Ideal for formal gallery receptions and red-carpet fittings.",
    val aiStatus: String = "Aura Engine: Posture ready. Fit mapping calibrated.",
    val futureRecommendations: List<String> = listOf(
        "Platform Satin Pumps", 
        "Art Deco Emerald Choker", 
        "Midnight Velvet Clutch"
    )
)
