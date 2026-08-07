package com.aura.feature.ai.inference

interface AIModel<I, O> {
    fun load()
    fun predict(input: I): O
    fun close()
}
