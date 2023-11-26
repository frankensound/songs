package com.frankensound.api.songs

import com.frankensound.models.RequestDTO
import com.frankensound.utils.database.TestDatabaseFactory
import com.frankensound.utils.database.configureTestEnvironment
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
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

    @Test
    fun testCreateSong() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.post("/songs") {
            contentType(ContentType.Application.Json)
            val request = Json.encodeToString(RequestDTO.serializer(), RequestDTO("key", RequestDTO.DetailDTO("artist", "title")))
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.Found, status)
        }
    }

    @Test
    fun testCreateSongInvalidBody() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.post("/songs") {
            contentType(ContentType.Application.Json)
            setBody("Invalid JSON")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}