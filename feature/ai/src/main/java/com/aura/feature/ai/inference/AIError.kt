package com.aura.feature.ai.inference

sealed class AIError(val msg: String, cause: Throwable? = null) : Exception(msg, cause) {
    object ModelNotFound : AIError("Model file 'models/fashion_detector.onnx' not found in assets.")
    class ModelLoadFailure(cause: Throwable) : AIError("Failed to load ONNX session: ${cause.localizedMessage}", cause)
    object InvalidModel : AIError("Loaded ONNX model does not have expected input/output shapes.")
    class InferenceFailure(cause: Throwable) : AIError("Model inference failed: ${cause.localizedMessage}", cause)
    object ModelUnavailable : AIError("MODEL_UNAVAILABLE: Real on-device fashion segmentation model is not bundled in assets.")
    object NoGarmentDetected : AIError("NO_GARMENT_DETECTED: No garment detected in reference image above threshold.")
    object LowConfidence : AIError("LOW_CONFIDENCE: Garment detection confidence is too low.")
    object UnsupportedImage : AIError("UNSUPPORTED_IMAGE: Unsupported image format or unreadable Uri.")
    object ProcessingFailed : AIError("PROCESSING_FAILED: Garment extraction processing failed.")
}
