package com.aura.feature.rendering.domain

import android.view.SurfaceView
import com.aura.feature.ai.model.PoseLandmarks
import com.aura.feature.ai.domain.ParsedGarment

interface ArRenderer {
    /**
     * Attaches Filament/SceneView renderer to the surface.
     */
    fun attachSurface(surfaceView: SurfaceView)
    
    /**
     * Detaches surface on lifecycle pause.
     */
    fun detachSurface()

    /**
     * Deforms the 3D model/mesh to align with detected human joints.
     */
    fun updatePose(pose: PoseLandmarks)

    /**
     * Dynamically loads a garment texture overlay onto the mesh.
     */
    fun updateGarmentTexture(garment: ParsedGarment)

    /**
     * Configures the lighting environment, camera projection matrices, and mesh materials.
     */
    fun setRenderingParameters(brightness: Float, smoothingFactor: Float)
}
