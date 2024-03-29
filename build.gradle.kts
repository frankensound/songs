val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometheus_version: String by project
val exposed_version: String by project
val h2_version: String by project
val postgresql_version: String by project
val hikari_version: String by project
val rabbitmq_version: String by project
val aws_version: String by project

plugins {
    kotlin("jvm") version "1.9.21"
    id("io.ktor.plugin") version "2.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
    id("org.sonarqube") version "4.4.1.3373"
}

group = "com.frankensound"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

sonar {
    properties {
        property("sonar.projectKey", "frankensound_songs")
        property("sonar.organization", "frankensound")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")

    // JSON serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // Metrics
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")

    // Exposed (Kotlin SQL framework)
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    // In-memory database driver
    implementation("com.h2database:h2:$h2_version")

    // PostgreSQL JDBC Driver
    implementation("org.postgresql:postgresql:$postgresql_version")

    // Database connection pooling
    implementation("com.zaxxer:HikariCP:$hikari_version")

    // Messaging
    implementation("com.rabbitmq:amqp-client:$rabbitmq_version")

    // AWS S3
    implementation("aws.sdk.kotlin:s3:$aws_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    //Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
