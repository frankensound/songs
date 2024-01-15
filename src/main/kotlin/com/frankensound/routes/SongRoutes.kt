package com.frankensound.routes

import com.frankensound.models.*
import com.frankensound.models.SongData.Companion.serialized
import com.frankensound.services.SongService
import com.frankensound.utils.messaging.RabbitMQManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val songService = SongService()

fun Route.songRouting() {
    route("/songs") {
        get {
            if (songService.getAll().isNotEmpty()) {
                call.respond(mapOf("songs" to songService.getAll()))
            } else {
                call.respondText("No songs found", status = HttpStatusCode.NotFound)
            }
        }
        get("{key?}") {
            val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing key.")
            val userId = call.request.headers["UserID"]

            if (userId != null) {
                val song = songService.get(key)
                if (song != null) {
                    val historyQueue = this@route.environment!!.config.property("ktor.rabbitmq.queue.history").getString()
                    val message = HistoryMessage(
                        userId = userId,
                        songId = key
                    )
                    RabbitMQManager.publishMessage(historyQueue, createJsonString(message))
                    call.respond(mapOf("song" to song.serialized()))
                } else {
                    call.respond(HttpStatusCode.NotFound, "No song with key $key found.")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "You don't seem to be logged in!")
            }
        }
        post<RequestDTO> { request ->
            val detail = DetailData(request.detailDto.artistName, request.detailDto.songTitle)
            val song = songService.create(request.key, detail)
            val key = song.key
            val userId = call.request.headers["UserID"]

            if (userId != null) {
                val eventsQueue = this@route.environment!!.config.property("ktor.rabbitmq.queue.events").getString()
                val message = EventMessage(
                    userId = userId,
                    actionType = "createdSong",
                    objectId = key
                )
                RabbitMQManager.publishMessage(eventsQueue, createJsonString(message))

                call.respondRedirect("/songs/$key")
            } else {
                call.respond(HttpStatusCode.BadRequest, "You don't seem to be logged in!")
            }

        }
        put<RequestDTO>("{key}") { request ->
            val key = call.parameters["key"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                "Missing or invalid song key."
            )
            val detail = DetailData(request.detailDto.artistName, request.detailDto.songTitle)
            val updated = songService.update(key, detail)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Song updated successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Song not found")
            }
        }
        delete("{key}") {
            val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing key.")
            if (songService.delete(key)) {
                call.respond(HttpStatusCode.Accepted, "Song removed correctly.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Song not deleted.")
            }
        }
    }
}