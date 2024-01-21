package com.frankensound.utils.database

import com.frankensound.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
    fun init(url: String, user: String, pass: String, driver: String) {
        val config = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = driver
            username = user
            password = pass
            maximumPoolSize = 50

            idleTimeout = 10000

            maxLifetime = 1800000

            connectionTimeout = 30000
            
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)
        val database =  Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(Songs, Details)
        }
    }
}