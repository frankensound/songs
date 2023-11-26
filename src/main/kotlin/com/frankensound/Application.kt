package com.frankensound

import com.frankensound.plugins.*
import com.frankensound.utils.database.databaseModule
import com.frankensound.utils.messaging.RabbitMQManager
import com.frankensound.utils.messaging.messagingModule
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    messagingModule()
    // Subscribe to ApplicationStopping event to close RabbitMQ connection
    environment.monitor.subscribe(ApplicationStopping) {
        RabbitMQManager.close()
    }
    databaseModule()
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics){
        registry = appMicrometerRegistry
    }
    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
    configureSerialization()
    configureRouting()
}
