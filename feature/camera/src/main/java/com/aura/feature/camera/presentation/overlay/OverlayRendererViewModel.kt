package com.aura.feature.camera.presentation.overlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.vision.pipeline.VisionPipeline
import com.aura.core.vision.model.PipelineState
import com.aura.core.vision.model.PipelineResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverlayRendererViewModel @Inject constructor(
    private val visionPipeline: VisionPipeline,
    private val overlayManager: OverlayManager
) : ViewModel() {

    val overlayState: StateFlow<OverlayState> = overlayManager.state

    init {
        // Observe pipeline state to show progress / base guides / status
        viewModelScope.launch {
            visionPipeline.state.collectLatest { state ->
                handlePipelineState(state)
            }
        }

        // Observe pipeline results to display dynamic AI elements
        viewModelScope.launch {
            visionPipeline.results.collectLatest { result ->
                handlePipelineResult(result)
            }
        }
    }

    private fun handlePipelineState(state: PipelineState) {
        val baseElements = mutableListOf<OverlayElement>(
            FaceGuideElement(),
            ShoulderGuideElement()
        )

        when (state) {
            is PipelineState.Idle -> {
                overlayManager.updateElements(baseElements)
            }
            is PipelineState.ReceivingFrames -> {
                baseElements.add(AiStatusBubbleElement(statusMessage = "Analyzing Posture..."))
                overlayManager.updateElements(baseElements)
            }
            is PipelineState.Processing -> {
                val elements = baseElements + listOf(
                    ProgressIndicatorElement(),
                    AiStatusBubbleElement(statusMessage = "Running AI Engines...")
                )
                overlayManager.updateElements(elements)
            }
            is PipelineState.Completed -> {
                baseElements.add(AiStatusBubbleElement(statusMessage = "Analysis Complete"))
                overlayManager.updateElements(baseElements)
            }
            is PipelineState.Error -> {
                val errorMsg = state.exception.localizedMessage ?: "Unknown error"
                baseElements.add(AiStatusBubbleElement(statusMessage = "Error: $errorMsg"))
                overlayManager.updateElements(baseElements)
            }
        }
    }

    private fun handlePipelineResult(result: PipelineResult) {
        val elements = mutableListOf<OverlayElement>()

        // 1. Base guides
        elements.add(FaceGuideElement())
        elements.add(ShoulderGuideElement())

        // 2. Pose -> Body outline
        if (result.pose != null) {
            elements.add(BodyOutlineElement())
        }

        // 3. Tracking -> Bounding box
        if (result.tracking != null && result.tracking!!.isTracking) {
            elements.add(
                TrackingBoxElement(
                    targetOffset = Offset(220f, 380f),
                    targetSize = Size(360f, 560f)
                )
            )
        }

        // 4. Style result -> Style Score Bubble
        if (result.style != null) {
            val scorePercent = (result.style!!.confidence * 100).toInt()
            elements.add(StyleScoreBubbleElement(score = scorePercent))
            elements.add(
                AiStatusBubbleElement(
                    statusMessage = "Style: ${result.style!!.primaryColor} - ${result.style!!.pattern}"
                )
            )
        } else {
            elements.add(AiStatusBubbleElement(statusMessage = "Scanning Fit..."))
        }

        // 5. Garment Bounding Boxes
        if (result.segmentation != null) {
            elements.add(
                GarmentBoundingBoxElement(
                    id = "garment_upper",
                    label = "OUTFIT TOP",
                    rectOffset = Offset(240f, 400f),
                    rectSize = Size(320f, 220f)
                )
            )
            elements.add(
                GarmentBoundingBoxElement(
                    id = "garment_lower",
                    label = "OUTFIT BOTTOM",
                    rectOffset = Offset(260f, 630f),
                    rectSize = Size(280f, 280f)
                )
            )
        }

        // 6. Recommendation Bubble
        if (result.recommendations != null && result.recommendations!!.recommendedItemIds.isNotEmpty()) {
            elements.add(
                RecommendationBubbleElement(
                    recommendations = result.recommendations!!.recommendedItemIds
                )
            )
        }

        overlayManager.updateElements(elements)
    }
}
