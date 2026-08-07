package com.aura.feature.ai.segmentation

import android.graphics.Bitmap
import com.aura.feature.ai.inference.AIModel

interface SamSegmenter : AIModel<Bitmap, Bitmap>
