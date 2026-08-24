package com.aura.feature.ai.domain

import android.graphics.Bitmap
import com.aura.core.common.data.ReferenceImage
import com.aura.feature.ai.model.GarmentAsset

/**
 * Interface contract for extracting garments from reference images.
 */
interface GarmentExtractionEngine {

    /**
     * Executes garment detection and segmentation on the selected reference image.
     */
    suspend fun extractGarment(
        referenceImage: ReferenceImage,
        decodedImage: Bitmap
    ): Result<GarmentAsset>
}
