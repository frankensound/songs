package com.frankensound.routes

import com.frankensound.models.*
import com.frankensound.models.DetailData.Companion.serialized
import com.frankensound.models.SongData.Companion.serialized
import com.frankensound.services.SongService
import com.frankensound.utils.deleteS3Object
import com.frankensound.utils.messaging.RabbitMQManager
import com.frankensound.utils.putS3Object
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*

val songService = SongService()
val bucketName = System.getenv("BUCKET_NAME")

fun createEventMessageJson(userId: String, actionType: String, objectId: Int): String {
    val jsonObject = buildJsonObject {
        put("userId", userId)
        put("actionType", actionType)
        put("objectId", objectId)
    }
    return Json.encodeToString(JsonObject.serializer(), jsonObject)
}

fun createHistoryMessageJson(userId: String, songId: Int): String {
    val jsonObject = buildJsonObject {
        put("userId", userId)
        put("songId", songId)
    }
    return Json.encodeToString(JsonObject.serializer(), jsonObject)
}

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
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or invalid song id.")
            val userId = call.request.headers["UserID"]

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "You don't seem to be logged in!")
                return@get
            }

            val (song, detail) = songService.getWithDetails(id)

            if (song == null || detail == null) {
                call.respond(HttpStatusCode.NotFound, "No song or details found for key $id.")
                return@get
            }

            val response = SongWithDetails(song.serialized(), detail.serialized())

            val historyQueue = this@route.environment!!.config.property("ktor.rabbitmq.queue.history").getString()
            val message = createHistoryMessageJson(
                userId = userId,
                songId = id
            )
            RabbitMQManager.publishMessage(historyQueue, message)

            call.respond(response)
        }
        post() {
            try {
                val multipart = call.receiveMultipart()
                var detailDto: RequestDTO.DetailDTO? = null
                var fileBytes: ByteArray? = null
                var proceed = true

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "details") {
                                val detailJson = part.value
                                detailDto = try {
                                    Json.decodeFromString(RequestDTO.serializer(), detailJson).detailDto
                                } catch (e: Exception) {
                                    call.respond(HttpStatusCode.BadRequest, "Invalid details format: ${e.message}")
                                    proceed = false
                                    return@forEachPart
                                }
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "file") {
                                fileBytes = part.streamProvider().readBytes()
                                if (fileBytes!!.isEmpty()) {
                                    call.respond(HttpStatusCode.BadRequest, "File data is missing.")
                                    proceed = false
                                    return@forEachPart
                                }
                            }
                        }
                        else -> { application.log.warn("Received an unexpected part of type: ${part::class.simpleName}") }
                    }
                    part.dispose()
                }

                if (!proceed) return@post

                if (detailDto == null || fileBytes == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing required data (details or file).")
                    return@post
                }

                val userId = call.request.headers["UserID"]
                if (userId.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "User ID is missing or invalid.")
                    return@post
                }

                val objectKey = "songs/${UUID.randomUUID()}"
                putS3Object(bucketName, objectKey, fileBytes!!)

                val song = songService.create(objectKey, DetailData(detailDto!!.artistName, detailDto!!.songTitle, detailDto!!.genre), userId)

                val songId = song.id.value

                val eventsQueue = this@route.environment!!.config.property("ktor.rabbitmq.queue.events").getString()
                val message = createEventMessageJson(
                    userId = userId,
                    actionType = "createdSong",
                    objectId = songId
                )
                RabbitMQManager.publishMessage(eventsQueue, message)

                call.respond(HttpStatusCode.OK, "Song created successfully.")

            } catch (e: Exception) {
                application.log.error("Error handling song upload", e)
                call.respond(HttpStatusCode.InternalServerError, "An error occurred while processing the request.")
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

            val song = songService.get(id)
            if (song != null) {
                // Delete the S3 object
                deleteS3Object(bucketName, song.key)
                // Delete the song record from the database
                if (songService.delete(id)) {
                    call.respond(HttpStatusCode.Accepted, "Song removed correctly.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Could not delete. Song with id $id not found.")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Song not found.")
            }
        }
    }
}