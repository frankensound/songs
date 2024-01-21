package com.frankensound.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

object EventBus {
    private val listeners = mutableListOf<(JsonElement) -> Unit>()

    fun subscribe(listener: (JsonElement) -> Unit) {
        listeners.add(listener)
    }

   fun emit(event: JsonElement) {
        listeners.forEach { listener ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    listener(event)
                } catch (e: Exception) {
                    println("Error handling event: ${e.message}")
                }
            }
        }
    }
}