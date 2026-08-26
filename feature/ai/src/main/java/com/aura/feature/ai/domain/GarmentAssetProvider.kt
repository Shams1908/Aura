package com.aura.feature.ai.domain

import com.aura.feature.ai.model.GarmentAsset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class that stores and retrieves the currently selected reference garment asset.
 */
@Singleton
class GarmentAssetProvider @Inject constructor() {
    private var activeAsset: GarmentAsset? = null

    fun getGarmentAsset(): GarmentAsset? {
        return activeAsset
    }

    fun setGarmentAsset(asset: GarmentAsset?) {
        activeAsset = asset
        println("AURA_DEBUG: GarmentAssetProvider active asset set to: ${asset?.category ?: "null"}")
    }
}
