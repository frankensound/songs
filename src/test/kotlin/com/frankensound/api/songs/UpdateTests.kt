package com.frankensound.api.songs

import com.frankensound.models.DetailData
import com.frankensound.models.RequestDTO
import com.frankensound.routes.songService
import com.frankensound.utils.database.TestDatabaseFactory
import com.frankensound.utils.database.configureTestEnvironment
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class UpdateTests {
    @BeforeTest
    fun setup() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }
    @Test
    fun testUpdateSong() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        val key = "test"
        songService.create(key, DetailData("artist","title"))
        client.put("/songs/$key") {
            contentType(ContentType.Application.Json)
            val request = Json.encodeToString(RequestDTO.serializer(), RequestDTO("key", RequestDTO.DetailDTO("artist", "title")))
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testUpdateNonexistentSong() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        val key = "test"
        client.put("/songs/$key") {
            contentType(ContentType.Application.Json)
            val request = Json.encodeToString(RequestDTO.serializer(), RequestDTO("key", RequestDTO.DetailDTO("artist", "title")))
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}