package com.aura.feature.camera.presentation.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayManager @Inject constructor() {
    private val _state = MutableStateFlow(OverlayState())
    val state: StateFlow<OverlayState> = _state.asStateFlow()

    fun updateElements(newElements: List<OverlayElement>) {
        _state.update { it.copy(elements = newElements) }
    }

    fun addElement(element: OverlayElement) {
        _state.update { 
            val filtered = it.elements.filter { el -> el.id != element.id }
            it.copy(elements = filtered + element) 
        }
    }

    fun removeElement(id: String) {
        _state.update { it.copy(elements = it.elements.filter { el -> el.id != id }) }
    }

    fun clearElements() {
        _state.update { it.copy(elements = emptyList()) }
    }

    fun setLayerVisibility(layer: OverlayLayer, isVisible: Boolean) {
        _state.update {
            val layers = if (isVisible) it.activeLayers + layer else it.activeLayers - layer
            it.copy(activeLayers = layers)
        }
    }

    fun setOverlayVisibility(isVisible: Boolean) {
        _state.update { it.copy(isVisible = isVisible) }
    }
}
