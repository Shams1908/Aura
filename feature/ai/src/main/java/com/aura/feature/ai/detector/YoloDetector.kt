package com.aura.feature.ai.detector

import android.graphics.Bitmap
import com.aura.feature.ai.inference.AIModel
import com.aura.feature.ai.model.Detection

interface YoloDetector : AIModel<Bitmap, List<Detection>>
