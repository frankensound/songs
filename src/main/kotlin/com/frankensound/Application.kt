package com.frankensound

import com.frankensound.plugins.*
import com.frankensound.services.SongService
import com.frankensound.utils.database.databaseModule
import com.frankensound.utils.messaging.RabbitMQManager
import com.frankensound.utils.messaging.messagingModule
import com.frankensound.utils.s3Client
import io.ktor.server.application.*
import configureMetrics

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

object ServiceRegistry {
    val songService by lazy { SongService() }
}

fun Application.module() {
    configureMetrics()
    messagingModule()
    databaseModule()
    configureSerialization()
    configureRouting()



    // Subscribe to ApplicationStopping event to close RabbitMQ and S3 connection
    environment.monitor.subscribe(ApplicationStopping) {
        RabbitMQManager.close()
        s3Client.close()
        ServiceRegistry.songService.close()
    }
}
