package com.aura.feature.ai.util

import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val env = OrtEnvironment.getEnvironment()
    private val sessionsLock = ReentrantLock()
    private val sessionsCache = mutableMapOf<String, OrtSession>()

    fun loadSession(modelPath: String): OrtSession {
        sessionsLock.withLock {
            val cached = sessionsCache[modelPath]
            if (cached != null) return cached

            val assetManager = context.assets
            val inputStream = assetManager.open(modelPath)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val session = env.createSession(bytes, OrtSession.SessionOptions())
            sessionsCache[modelPath] = session
            return session
        }
    }

    fun loadLabels(labelsPath: String): List<String> {
        val labels = mutableListOf<String>()
        val assetManager = context.assets
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelsPath)))
        var line = reader.readLine()
        while (line != null) {
            if (line.trim().isNotEmpty()) {
                labels.add(line.trim())
            }
            line = reader.readLine()
        }
        reader.close()
        return labels
    }

    fun closeSession(modelPath: String) {
        sessionsLock.withLock {
            val session = sessionsCache.remove(modelPath)
            session?.close()
        }
    }

    fun closeAll() {
        sessionsLock.withLock {
            for (session in sessionsCache.values) {
                session.close()
            }
            sessionsCache.clear()
        }
    }
}
