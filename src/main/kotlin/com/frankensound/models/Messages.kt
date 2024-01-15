package com.frankensound.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
abstract class BaseMessage

@Serializable
data class EventMessage(val userId: String, val actionType: String, val objectId: String) : BaseMessage()

@Serializable
data class HistoryMessage(val userId: String, val songId: String) : BaseMessage()

fun createJsonString(message: BaseMessage): String {
    return Json.encodeToString(message)
}
