package com.frankensound.api.songs

import com.frankensound.utils.database.TestDatabaseFactory
import com.frankensound.utils.database.configureTestEnvironment
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class CreateTests {
    @BeforeTest
    fun setup() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    //TODO: Create mock RabbitMQ service so that this function can be tested
    //TODO: Create mock S3 service so that this function can be tested

    @Test
    fun testCreateSongInvalidBody() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.post("/songs") {
            header("UserID", "test")
            contentType(ContentType.Application.Json)
            setBody("Invalid JSON")
        }.apply {
            assertEquals(HttpStatusCode.InternalServerError, status)
        }
    }
}