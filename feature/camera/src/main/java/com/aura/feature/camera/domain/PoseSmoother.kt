package com.aura.feature.camera.domain

import androidx.compose.ui.geometry.Offset

class PointSmoother(private val alpha: Float = 0.35f) {
    private var prevX: Float? = null
    private var prevY: Float? = null

    fun smooth(x: Float, y: Float): Offset {
        val px = prevX
        val py = prevY
        val smoothedX = if (px == null) x else px + alpha * (x - px)
        val smoothedY = if (py == null) y else py + alpha * (y - py)
        prevX = smoothedX
        prevY = smoothedY
        return Offset(smoothedX, smoothedY)
    }

    fun reset() {
        prevX = null
        prevY = null
    }
}

class PoseSmoother(private val alpha: Float = 0.35f) {
    private val smoothers = mutableMapOf<Int, PointSmoother>()

    fun smooth(id: Int, x: Float, y: Float): Offset {
        val smoother = smoothers.getOrPut(id) { PointSmoother(alpha) }
        return smoother.smooth(x, y)
    }

    fun reset() {
        smoothers.clear()
    }
}
