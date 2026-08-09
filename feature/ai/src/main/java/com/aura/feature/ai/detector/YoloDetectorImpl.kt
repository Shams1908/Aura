package com.aura.feature.ai.detector

import android.graphics.Bitmap
import android.graphics.RectF
import com.aura.feature.ai.inference.AIError
import com.aura.feature.ai.model.Detection
import com.aura.feature.ai.preprocessing.ImagePreprocessor
import com.aura.feature.ai.util.ModelLoader
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoloDetectorImpl @Inject constructor(
    private val modelLoader: ModelLoader,
    private val preprocessor: ImagePreprocessor
) : YoloDetector {

    private var session: OrtSession? = null
    private var labels = listOf<String>()
    private var isLoaded = false

    override fun load() {
        if (isLoaded) return
        try {
            session = modelLoader.loadSession("models/fashion_detector.onnx")
            labels = modelLoader.loadLabels("models/fashion_labels.txt")
            
            val sess = session ?: throw IllegalStateException("Session is null")
            
            // Validate input and output shapes
            val inputInfo = sess.inputInfo
            val outputInfo = sess.outputInfo
            
            val nodeInfo = inputInfo.values.firstOrNull()
            val tensorInfo = nodeInfo?.info as? TensorInfo
            val inputShape = tensorInfo?.shape
            
            val outNodeInfo = outputInfo.values.firstOrNull()
            val outTensorInfo = outNodeInfo?.info as? TensorInfo
            val outputShape = outTensorInfo?.shape
            
            if (inputShape == null || outputShape == null ||
                inputShape.size != 4 || inputShape[2] != 640L || inputShape[3] != 640L ||
                outputShape.size != 3 || outputShape[1] != 50L || outputShape[2] != 8400L) {
                throw AIError.InvalidModel
            }
            
            isLoaded = true
        } catch (e: java.io.FileNotFoundException) {
            isLoaded = false
            throw AIError.ModelNotFound
        } catch (e: AIError) {
            isLoaded = false
            throw e
        } catch (e: Exception) {
            isLoaded = false
            throw AIError.ModelLoadFailure(e)
        }
    }

    override fun predict(input: Bitmap): List<Detection> {
        if (!isLoaded || session == null) {
            throw IllegalStateException("Model is not loaded.")
        }

        try {
            val targetWidth = 640
            val targetHeight = 640
            val floatBuffer = preprocessor.preprocessForYolo(input, targetWidth, targetHeight)
            val shape = longArrayOf(1, 3, targetWidth.toLong(), targetHeight.toLong())
            val env = OrtEnvironment.getEnvironment()
            
            val inputTensor = OnnxTensor.createTensor(env, floatBuffer, shape)
            val inputs = mapOf("images" to inputTensor)
            
            val output = session?.run(inputs) ?: throw AIError.InferenceFailure(IllegalStateException("Session run returned null"))
            val outputTensor = output.get(0) as? OnnxTensor ?: throw AIError.InferenceFailure(IllegalStateException("Output is not an OnnxTensor"))
            
            val detections = decodeOutput(outputTensor, input.width, input.height)
            inputTensor.close()
            output.close()
            
            return detections
        } catch (e: Exception) {
            if (e is AIError) throw e
            throw AIError.InferenceFailure(e)
        }
    }

    override fun close() {
        modelLoader.closeSession("models/fashion_detector.onnx")
        session = null
        isLoaded = false
    }

    private fun decodeOutput(outputTensor: OnnxTensor, srcWidth: Int, srcHeight: Int): List<Detection> {
        val rawData = outputTensor.floatBuffer
        val shape = outputTensor.info.shape
        val numChannels = shape[1].toInt()
        val numAnchors = shape[2].toInt()
        
        val confidenceThreshold = 0.35f
        val candidateDetections = mutableListOf<Detection>()
        
        // Letterbox params to map coordinates back to original image
        val targetWidth = 640f
        val targetHeight = 640f
        val scale = minOf(targetWidth / srcWidth, targetHeight / srcHeight)
        val dx = (targetWidth - srcWidth * scale) / 2f
        val dy = (targetHeight - srcHeight * scale) / 2f

        for (anchorIdx in 0 until numAnchors) {
            var maxConfidence = 0.0f
            var maxClassId = -1
            
            for (classId in 0 until (numChannels - 4)) {
                val index = (4 + classId) * numAnchors + anchorIdx
                if (index < rawData.limit()) {
                    val conf = rawData.get(index)
                    if (conf > maxConfidence) {
                        maxConfidence = conf
                        maxClassId = classId
                    }
                }
            }
            
            if (maxConfidence >= confidenceThreshold && maxClassId != -1 && maxClassId < labels.size) {
                val xc = rawData.get(0 * numAnchors + anchorIdx)
                val yc = rawData.get(1 * numAnchors + anchorIdx)
                val w = rawData.get(2 * numAnchors + anchorIdx)
                val h = rawData.get(3 * numAnchors + anchorIdx)
                
                // Undo letterbox padding and scale back to original dimensions
                val x1 = ((xc - w / 2f) - dx) / scale
                val y1 = ((yc - h / 2f) - dy) / scale
                val x2 = ((xc + w / 2f) - dx) / scale
                val y2 = ((yc + h / 2f) - dy) / scale
                
                val rect = RectF(
                    maxOf(0f, x1),
                    maxOf(0f, y1),
                    minOf(srcWidth.toFloat(), x2),
                    minOf(srcHeight.toFloat(), y2)
                )
                
                candidateDetections.add(
                    Detection(
                        label = labels[maxClassId],
                        classId = maxClassId,
                        confidence = maxConfidence,
                        boundingBox = rect
                    )
                )
            }
        }
        
        return applyNms(candidateDetections)
    }

    private fun applyNms(boxes: List<Detection>): List<Detection> {
        val selected = mutableListOf<Detection>()
        val boxesByClass = boxes.groupBy { it.classId }
        for ((_, classBoxes) in boxesByClass) {
            val sorted = classBoxes.sortedByDescending { it.confidence }.toMutableList()
            while (sorted.isNotEmpty()) {
                val first = sorted.removeAt(0)
                selected.add(first)
                val iterator = sorted.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (calculateIou(first.boundingBox, next.boundingBox) > 0.45f) {
                        iterator.remove()
                    }
                }
            }
        }
        return selected
    }

    private fun calculateIou(a: RectF, b: RectF): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)

        if (left >= right || top >= bottom) return 0f

        val intersectionArea = (right - left) * (bottom - top)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val unionArea = areaA + areaB - intersectionArea
        return if (unionArea > 0f) intersectionArea / unionArea else 0f
    }
}
