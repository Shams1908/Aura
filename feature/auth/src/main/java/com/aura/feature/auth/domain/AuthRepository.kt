package com.aura.feature.auth.domain

import kotlinx.coroutines.flow.Flow

data class UserSession(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

interface AuthRepository {
    val currentUserSession: Flow<UserSession?>
    
    suspend fun loginWithGoogle(idToken: String): Result<UserSession>
    
    suspend fun loginWithEmail(email: String, password: String): Result<UserSession>
    
    suspend fun signupWithEmail(email: String, password: String): Result<UserSession>
    
    suspend fun logout(): Result<Unit>
}
