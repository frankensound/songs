package com.frankensound.api.songs

import com.frankensound.models.DetailData
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
        val song = songService.create(key, DetailData("artist", "title", "genre"), "test")
        val songId = song.id.value
        client.put("/songs/$songId") {
            header("UserID", "test")
            contentType(ContentType.Application.Json)
            val detailData = DetailData("updated artist", "updated title", "updated genre")
            setBody(Json.encodeToString(DetailData.serializer(), detailData))
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
        val songId = 1245
        client.put("/songs/$songId") {
            header("UserID", "test")
            contentType(ContentType.Application.Json)
            val detailData = DetailData("artist", "title", "genre")
            setBody(Json.encodeToString(DetailData.serializer(), detailData))
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}