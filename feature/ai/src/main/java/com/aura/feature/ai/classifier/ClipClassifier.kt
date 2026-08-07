package com.aura.feature.ai.classifier

import android.graphics.Bitmap
import com.aura.feature.ai.inference.AIModel

interface ClipClassifier : AIModel<Bitmap, FloatArray>
