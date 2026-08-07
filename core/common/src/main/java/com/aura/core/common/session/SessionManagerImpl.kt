package com.aura.core.common.session

import com.aura.core.common.data.OutfitModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Standard implementation of [SessionManager].
 */
@Singleton
class SessionManagerImpl @Inject constructor(
    private val repository: SessionRepository
) : SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _activeSession = MutableStateFlow<OutfitSession?>(null)
    override val activeSession: StateFlow<OutfitSession?> = _activeSession.asStateFlow()

    init {
        scope.launch {
            repository.getActiveSessionId().collectLatest { activeId ->
                android.util.Log.d("AURA_DEBUG", "SessionManagerImpl active ID collected: $activeId")
                if (activeId != null) {
                    repository.getSession(activeId).collectLatest { session ->
                        android.util.Log.d("AURA_DEBUG", "SessionManagerImpl active session collected: $session")
                        _activeSession.value = session
                    }
                } else {
                    android.util.Log.d("AURA_DEBUG", "SessionManagerImpl active session is null")
                    _activeSession.value = null
                }
            }
        }
    }

    override suspend fun createNewSession(): OutfitSession {
        val newId = OutfitSessionId(UUID.randomUUID().toString())
        android.util.Log.d("AURA_DEBUG", "SessionManagerImpl.createNewSession: created session with ID = ${newId.value}")
        val session = OutfitSession(sessionId = newId)
        repository.saveSession(session)
        repository.setActiveSessionId(newId)
        return session
    }

    override suspend fun attachOutfit(outfitUri: String, metadata: OutfitModel) {
        val current = _activeSession.value ?: return
        val updated = current.copy(
            stage = SessionLifecycleStage.OUTFIT_ATTACHED,
            referenceOutfitUri = outfitUri,
            referenceOutfitMetadata = metadata,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun transitionStage(newStage: SessionLifecycleStage) {
        android.util.Log.d("AURA_DEBUG", "SessionManagerImpl.transitionStage: transitioning stage to $newStage")
        val current = _activeSession.value ?: return
        val updated = current.copy(
            stage = newStage,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun updateCameraState(state: String) {
        val current = _activeSession.value ?: return
        val updated = current.copy(
            cameraState = state,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun updateTrackingState(state: String) {
        val current = _activeSession.value ?: return
        val updated = current.copy(
            trackingState = state,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun updateAnalysisStatus(status: String) {
        val current = _activeSession.value ?: return
        val updated = current.copy(
            analysisStatus = status,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun updateTryOnStatus(status: String) {
        val current = _activeSession.value ?: return
        val updated = current.copy(
            tryOnStatus = status,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun updateShoppingStatus(status: String) {
        val current = _activeSession.value ?: return
        val updated = current.copy(
            shoppingStatus = status,
            updatedTime = System.currentTimeMillis()
        )
        repository.saveSession(updated)
    }

    override suspend fun completeSession() {
        android.util.Log.d("AURA_DEBUG", "SessionManagerImpl.completeSession called")
        repository.setActiveSessionId(null)
    }

    override suspend fun loadSession(sessionId: OutfitSessionId) {
        android.util.Log.d("AURA_DEBUG", "SessionManagerImpl.loadSession: loading session ID = ${sessionId.value}")
        repository.setActiveSessionId(sessionId)
    }
}
