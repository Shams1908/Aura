package com.aura.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.core.security.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, authCode: String) {
        if (email.isBlank() || authCode.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and Password cannot be blank.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(800) // Mock latency
            tokenStorage.saveAccessToken("mock_session_key_12345")
            _uiState.value = AuthUiState.Success
        }
    }

    fun signUp(name: String, email: String, authCode: String, confirmCode: String) {
        if (name.isBlank() || email.isBlank() || authCode.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required.")
            return
        }
        if (authCode != confirmCode) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(1000) // Mock latency
            tokenStorage.saveAccessToken("mock_session_key_12345")
            _uiState.value = AuthUiState.Success
        }
    }

    fun logout() {
        tokenStorage.clearSession()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
