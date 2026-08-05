package com.aura.feature.camera.presentation.components

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.aura.feature.camera.domain.CameraController

/**
 * Compose layout hosting CameraX PreviewView inside interop containers.
 */
@Composable
fun CameraPreview(
    controller: CameraController,
    onPreviewViewCreated: (PreviewView) -> Unit,
    modifier: Modifier = Modifier,
    onTap: (Float, Float) -> Unit = { _, _ -> }
) {
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                onPreviewViewCreated(this)
            }
        },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        onTap(offset.x, offset.y)
                    }
                )
            },
        update = { _ -> }
    )
}
