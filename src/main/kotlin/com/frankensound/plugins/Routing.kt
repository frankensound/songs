package com.frankensound.plugins

import com.frankensound.routes.songRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        songRouting()
        healthRouting()
    }
}
fun Route.healthRouting() {
    get("/healthz") {
        call.respondText("OK", ContentType.Text.Plain)
    }
}
