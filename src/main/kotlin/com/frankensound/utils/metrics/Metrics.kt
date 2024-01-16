import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.Histogram

object MetricsRegistry {
    val requestDurationHistogram: Histogram = Histogram.build()
        .name("request_duration_milliseconds")
        .help("Request duration in milliseconds.")
        .labelNames("service")
        .buckets(0.1, 1.0, 5.0, 10.0, 25.0, 50.0, 100.0, 200.0, 500.0) // Define buckets in milliseconds
        .register()
}

fun Application.configureRequestDurationRecording() {
    intercept(ApplicationCallPipeline.Monitoring) {
        val start = System.nanoTime()
        try {
            proceed()
        } finally {
            val duration = (System.nanoTime() - start) / 1_000_000.0 // Convert nanoseconds to milliseconds
            val serviceName = "songs"
            MetricsRegistry.requestDurationHistogram.labels(serviceName).observe(duration)
        }
    }
}

fun Application.configureMetrics() {
    configureRequestDurationRecording()

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
}