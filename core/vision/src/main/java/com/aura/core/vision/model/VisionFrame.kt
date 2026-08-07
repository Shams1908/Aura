package com.aura.core.vision.model

import android.graphics.Bitmap

data class VisionFrame(
    val bitmap: Bitmap,
    val metadata: FrameMetadata
)
