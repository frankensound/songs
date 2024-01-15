package com.frankensound.routes

import com.frankensound.models.*
import com.frankensound.models.SongData.Companion.serialized
import com.frankensound.services.SongService
import com.frankensound.utils.messaging.RabbitMQManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
        get("{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or invalid song id.")
            val userId = call.request.headers["UserID"]

            if (userId != null) {
                val song = songService.get(id)
                if (song != null) {
                    val historyQueue = this@route.environment!!.config.property("ktor.rabbitmq.queue.history").getString()
                    val message = HistoryMessage(
                        userId = userId,
                        songId = id
                    )
                    RabbitMQManager.publishMessage(historyQueue, createJsonString(message))
                    call.respond(mapOf("song" to song.serialized()))
                } else {
                    call.respond(HttpStatusCode.NotFound, "No song with key $id found.")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "You don't seem to be logged in!")
            }
        }
        post<RequestDTO> { request ->
            val detail = DetailData(request.detailDto.artistName, request.detailDto.songTitle, request.detailDto.genre)
            val userId = call.request.headers["UserID"]

            if (userId != null) {
                val song = songService.create(request.key, detail, userId)
                val songId = song.id.value

                val eventsQueue = this@route.environment!!.config.property("ktor.rabbitmq.queue.events").getString()
                val message = EventMessage(
                    userId = userId,
                    actionType = "createdSong",
                    objectId = songId
                )
                RabbitMQManager.publishMessage(eventsQueue, createJsonString(message))

                call.respondRedirect("/songs/$songId")
            } else {
                call.respond(HttpStatusCode.BadRequest, "You don't seem to be logged in!")
            }

        }
        put<RequestDTO>("{id}") { request ->
            val id = call.parameters["id"]?.toInt() ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                "Missing or invalid song id!"
            )
            val detail = DetailData(request.detailDto.artistName, request.detailDto.songTitle, request.detailDto.genre)
            val updated = songService.update(id, detail)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Song updated successfully.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Song with id $id not found.")
            }
        }
        delete("{id}") {
            val id = call.parameters["id"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing or invalid song id!")
            if (songService.delete(id)) {
                call.respond(HttpStatusCode.Accepted, "Song removed correctly.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Could not delete. Song with id $id not found.")
            }
        }
    }
}