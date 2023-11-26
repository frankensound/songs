package com.frankensound.utils.database

import com.frankensound.models.*
import com.frankensound.plugins.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*

object TestDatabaseFactory {
    fun init() {
        // Using an H2 in-memory database for testing
        val jdbcURL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
        val driverClassName = "org.h2.Driver"
        val database = Database.connect(jdbcURL, driverClassName)

        transaction(database) {
            // Create tables required for tests
            SchemaUtils.create(Songs, Details)
        }
    }

    fun cleanUp() {
        transaction {
            SchemaUtils.drop(Songs, Details)
        }
    }
}

fun Application.configureTestEnvironment() {
    // Initialize the test database
    TestDatabaseFactory.init()

    // Set up routing
    configureRouting()

    install(ContentNegotiation) {
        json()
    }
}