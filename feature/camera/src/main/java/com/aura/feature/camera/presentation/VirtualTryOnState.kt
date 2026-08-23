package com.aura.feature.camera.presentation

sealed interface VirtualTryOnState {
    data object Idle : VirtualTryOnState
    data object Preparing : VirtualTryOnState
    data object Generating : VirtualTryOnState
    data class Success(val resultImageUrl: String) : VirtualTryOnState
    data class Error(val errorMsg: String) : VirtualTryOnState
    data object Cancelled : VirtualTryOnState
}
