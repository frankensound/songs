package com.frankensound.utils

import kotlinx.serialization.json.JsonElement

object EventBus {
    private val listeners = mutableListOf<(JsonElement) -> Unit>()

    fun subscribe(listener: (JsonElement) -> Unit) {
        listeners.add(listener)
    }

    fun emit(event: JsonElement) {
        listeners.forEach { it(event) }
    }
}