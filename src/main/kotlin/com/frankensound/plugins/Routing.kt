package com.frankensound.plugins

import com.frankensound.routes.songRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        songRouting()
    }
}
