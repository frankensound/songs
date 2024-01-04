package com.frankensound

import com.frankensound.plugins.*
import com.frankensound.utils.database.databaseModule
import com.frankensound.utils.messaging.RabbitMQManager
import com.frankensound.utils.messaging.messagingModule
import io.ktor.server.application.*
import configureMetrics

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureMetrics()
    messagingModule()
    databaseModule()
    configureSerialization()
    configureRouting()
    // Subscribe to ApplicationStopping event to close RabbitMQ connection
    environment.monitor.subscribe(ApplicationStopping) {
        RabbitMQManager.close()
    }
}
