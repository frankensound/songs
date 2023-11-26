package com.frankensound.utils

import com.frankensound.models.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*

fun Application.databaseModule() {
    val config = environment.config
    val dbUrl = config.property("ktor.database.url").getString()
    val dbUser = config.property("ktor.database.user").getString()
    val dbPassword = config.property("ktor.database.password").getString()
    val dbDriver = config.property("ktor.database.driver").getString()

    DatabaseFactory.init(dbUrl, dbUser, dbPassword, dbDriver)
}

object DatabaseFactory {
    fun init(url: String, user: String, password: String, driver: String) {
        val database =  Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        transaction(database) {
            SchemaUtils.create(Songs, Details)
        }
    }
}