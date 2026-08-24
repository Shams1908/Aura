package com.aura.feature.ai.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.feature.ai.data.FakeOutfitWorkspaceRepository
import com.aura.feature.ai.domain.model.WorkspaceAction
import com.aura.feature.ai.domain.GarmentExtractionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aura.core.common.session.SessionManager
import com.aura.core.common.session.OutfitSessionId

/**
 * ViewModel for the Outfit Workspace feature managing states and action triggers.
 */
@HiltViewModel
class OutfitWorkspaceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FakeOutfitWorkspaceRepository,
    private val sessionManager: SessionManager,
    private val garmentExtractionEngine: GarmentExtractionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<OutfitWorkspaceUiState>(OutfitWorkspaceUiState.Loading)
    val uiState: StateFlow<OutfitWorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadWorkspace()
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionManager.activeSession.collect { session ->
                if (session != null) {
                    val uri = session.referenceOutfitUri
                    if (uri != null) {
                        selectImageInternal(session)
                    } else {
                        removeImageInternal()
                    }
                }
            }
        }
    }

    fun loadSession(sessionId: OutfitSessionId) {
        viewModelScope.launch {
            sessionManager.loadSession(sessionId)
        }
    }

    /**
     * Initializes workspace settings.
     */
    fun loadWorkspace() {
        _uiState.value = OutfitWorkspaceUiState.Success(
            tips = repository.getTips()
        )
    }

    /**
     * Actions when selecting/uploading a mock image.
     */
    fun selectImage(uri: String) {
        viewModelScope.launch {
            sessionManager.attachOutfit(
                outfitUri = uri,
                metadata = com.aura.core.common.data.OutfitModel(
                    id = System.currentTimeMillis().toString(),
                    title = "Workspace Outfit",
                    brand = "Workspace",
                    description = "Visual fit analysis item",
                    imageUrl = uri,
                    category = "Clothing",
                    color = "Default",
                    tags = listOf("Workspace"),
                    price = 0.0
                )
            )
        }
    }

    private fun selectImageInternal(session: com.aura.core.common.session.OutfitSession) {
        val uri = session.referenceOutfitUri ?: return
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success
            ?: OutfitWorkspaceUiState.Success(tips = repository.getTips())
        if (currentState.selectedImageUri == uri && currentState.referenceImage == session.referenceImage) return

        val refImage = session.referenceImage
        val resolvedFilename = when (refImage?.source) {
            com.aura.core.common.data.ReferenceImageSource.USER_DEVICE_GALLERY -> {
                val title = refImage.metadata?.title
                if (!title.isNullOrBlank()) title else "Selected Outfit"
            }
            com.aura.core.common.data.ReferenceImageSource.USER_FILE_PICKER -> {
                val title = refImage.metadata?.title
                if (!title.isNullOrBlank()) title else "Selected Outfit"
            }
            else -> when {
                uri.contains("1556821840") -> "casual_hoodie_outfit.jpg"
                uri.contains("1515886657") -> "model_summer_wear.png"
                else -> {
                    val title = refImage?.metadata?.title
                    if (!title.isNullOrBlank()) title else "Selected Outfit"
                }
            }
        }
        val resolvedDimensions = refImage?.metadata?.let {
            val size = it.sizeBytes
            if (size != null) {
                val kb = size / 1024.0
                if (kb > 1024) {
                    String.format(java.util.Locale.US, "%.2f MB", kb / 1024.0)
                } else {
                    String.format(java.util.Locale.US, "%.1f KB", kb)
                }
            } else null
        } ?: "1.2 MB"

        val resolvedRefImage = refImage ?: com.aura.core.common.data.ReferenceImage(
            uri = uri,
            source = com.aura.core.common.data.ReferenceImageSource.DEFAULT_GALLERY,
            metadata = com.aura.core.common.data.ReferenceImageMetadata(title = "Selected Outfit")
        )

        _uiState.value = currentState.copy(
            selectedImageUri = uri,
            isProcessing = true,
            detectedItems = emptyList(),
            detectedStyle = null,
            filename = resolvedFilename,
            dimensions = resolvedDimensions,
            infoMessage = null,
            referenceImage = resolvedRefImage
        )

        viewModelScope.launch {
            val bitmap = decodeBitmapFromUri(context, uri)
            if (bitmap == null) {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(
                            isProcessing = false,
                            infoMessage = "Failed to load/decode reference image."
                        )
                    } else state
                }
                return@launch
            }

            // Trigger extraction on-demand (exactly once)
            val result = garmentExtractionEngine.extractGarment(resolvedRefImage, bitmap)
            result.fold(
                onSuccess = { asset ->
                    _uiState.update { state ->
                        if (state is OutfitWorkspaceUiState.Success) {
                            state.copy(
                                isProcessing = false,
                                detectedItems = listOf(
                                    com.aura.feature.ai.domain.model.DetectedClothing(
                                        id = "ext_1",
                                        name = asset.category,
                                        confidence = asset.confidence
                                    )
                                )
                            )
                        } else state
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("AURA_DEBUG", "Garment extraction error: ${error.message}")
                    _uiState.update { state ->
                        if (state is OutfitWorkspaceUiState.Success) {
                            state.copy(
                                isProcessing = false,
                                infoMessage = error.message ?: "Garment extraction failed"
                            )
                        } else state
                    }
                }
            )
        }
    }

    /**
     * Clears selected workspace picture.
     */
    fun removeImage() {
        viewModelScope.launch {
            sessionManager.completeSession()
        }
        removeImageInternal()
    }

    private fun removeImageInternal() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success
            ?: OutfitWorkspaceUiState.Success(tips = repository.getTips())
        _uiState.value = currentState.copy(
            selectedImageUri = null,
            isProcessing = false,
            detectedItems = emptyList(),
            detectedStyle = null,
            filename = null,
            dimensions = null,
            infoMessage = null,
            referenceImage = null
        )
    }

    /**
     * Triggers simulated AI actions based on [WorkspaceAction] enums.
     */
    fun executeAction(action: WorkspaceAction) {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        val imageUri = currentState.selectedImageUri
        
        if (imageUri == null) {
            _uiState.update { state ->
                if (state is OutfitWorkspaceUiState.Success) {
                    state.copy(infoMessage = "Please upload an outfit image first.")
                } else {
                    state
                }
            }
            return
        }

        when (action) {
            WorkspaceAction.ANALYZE -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(isProcessing = true, infoMessage = null)
                    } else {
                        state
                    }
                }
                viewModelScope.launch {
                    repository.analyzeStyle(imageUri).collect { result ->
                        result.fold(
                            onSuccess = { style ->
                                _uiState.update { state ->
                                    if (state is OutfitWorkspaceUiState.Success) {
                                        state.copy(
                                            isProcessing = false,
                                            detectedStyle = style,
                                            infoMessage = "AI styling analysis completed successfully."
                                        )
                                    } else {
                                        state
                                    }
                                }
                            },
                            onFailure = { error ->
                                _uiState.update { state ->
                                    if (state is OutfitWorkspaceUiState.Success) {
                                        state.copy(
                                            isProcessing = false,
                                            infoMessage = "Error analyzing style: ${error.localizedMessage}"
                                        )
                                    } else {
                                        state
                                    }
                                }
                            }
                        )
                    }
                }
            }
            WorkspaceAction.TRY_ON -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(infoMessage = "Virtual Try-On simulation started. Preparing avatars.")
                    } else {
                        state
                    }
                }
            }
            WorkspaceAction.FIND_SIMILAR -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(infoMessage = "Searching visual catalogs for matches.")
                    } else {
                        state
                    }
                }
            }
            WorkspaceAction.SAVE -> {
                _uiState.update { state ->
                    if (state is OutfitWorkspaceUiState.Success) {
                        state.copy(infoMessage = "Outfit workspace layout configuration saved.")
                    } else {
                        state
                    }
                }
            }
        }
    }

    /**
     * Dismisses active notification dialog message.
     */
    fun dismissInfo() {
        val currentState = _uiState.value as? OutfitWorkspaceUiState.Success ?: return
        _uiState.value = currentState.copy(infoMessage = null)
    }
}

private fun decodeBitmapFromUri(context: android.content.Context, uriString: String): Bitmap? {
    return try {
        val uri = android.net.Uri.parse(uriString)
        if (uri.scheme == "http" || uri.scheme == "https") {
            Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888)
        } else {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("AURA_DEBUG", "Failed to decode bitmap from URI: $uriString", e)
        null
    }
}
