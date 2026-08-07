package com.aura.feature.camera.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.aura.core.vision.provider.FrameProvider
import com.aura.core.vision.model.VisionFrame
import com.aura.core.vision.model.FrameMetadata

/**
 * Controller class encapsulating all CameraX preview, capturing, zooming, and frame analyzing tasks.
 */
class CameraController(
    private val context: Context,
    private val frameProvider: FrameProvider
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isFlashEnabled = false
    private var zoomRatio = 1.0f

    /**
     * Obtains the CameraX singleton ProcessCameraProvider.
     */
    fun initialize(onInitialized: () -> Unit, onError: (Throwable) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                onInitialized()
            } catch (e: Exception) {
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Binds CameraX Preview, ImageCapture, and ImageAnalysis to the lifecycle of the parent owner.
     */
    fun bindUseCase(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onPhotoCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setFlashMode(if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .build()

        // Prepare ImageAnalysis use-case for future real-time posturing/alignment AI engines
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        val bitmap = imageProxy.toBitmap()
                        val metadata = FrameMetadata(
                            width = imageProxy.width,
                            height = imageProxy.height,
                            rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                            timestampMs = imageProxy.imageInfo.timestamp,
                            lensFacing = lensFacing
                        )
                        frameProvider.emitFrame(VisionFrame(bitmap, metadata))
                    } catch (e: Exception) {
                        Log.e("CameraController", "Error processing frame", e)
                    } finally {
                        imageProxy.close()
                    }
                }
            }

        try {
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
            camera?.cameraControl?.setZoomRatio(zoomRatio)
        } catch (e: Exception) {
            Log.e("CameraController", "Use case binding failed", e)
        }
    }

    /**
     * Switches LENS_FACING_FRONT/BACK.
     */
    fun switchCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onPhotoCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindUseCase(lifecycleOwner, previewView, onPhotoCaptured, onError)
    }

    /**
     * Sets flash mode and toggles torch state.
     */
    fun toggleFlash(enabled: Boolean) {
        isFlashEnabled = enabled
        imageCapture?.flashMode = if (enabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        camera?.cameraControl?.enableTorch(enabled)
    }

    /**
     * Sets zoom ratio.
     */
    fun setZoom(ratio: Float) {
        zoomRatio = ratio
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun getZoomRatio(): Float = zoomRatio

    fun getLensFacing(): Int = lensFacing

    /**
     * Configures auto-focus metering.
     */
    fun tapToFocus(previewView: PreviewView, x: Float, y: Float) {
        val cameraControl = camera?.cameraControl ?: return
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        cameraControl.startFocusAndMetering(action)
    }

    /**
     * Triggers capture callback and outputs to a cache URI.
     */
    fun capturePhoto(
        onPhotoSaved: (Uri) -> Unit,
        onCaptureError: (ImageCaptureException) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onCaptureError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Image capture not initialized", null))
            return
        }

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(context.cacheDir, name)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onPhotoSaved(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onCaptureError(exception)
                }
            }
        )
    }

    /**
     * Shutdown single thread executors.
     */
    fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}
