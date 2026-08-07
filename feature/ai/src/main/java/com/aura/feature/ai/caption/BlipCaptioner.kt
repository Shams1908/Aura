package com.aura.feature.ai.caption

import android.graphics.Bitmap
import com.aura.feature.ai.inference.AIModel

interface BlipCaptioner : AIModel<Bitmap, String>
