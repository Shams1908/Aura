package com.aura.feature.ai.model

import android.graphics.RectF

data class Detection(
    val label: String,
    val classId: Int,
    val confidence: Float,
    val boundingBox: RectF
)
