package com.frankensound.api.songs

import com.frankensound.models.DetailData
import com.frankensound.routes.songService
import com.frankensound.utils.database.TestDatabaseFactory
import com.frankensound.utils.database.configureTestEnvironment
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class GetAllTests {
    @BeforeTest
    fun setup() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }
    @Test
    fun testGetSongs() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        songService.create("test", DetailData("artist","title", "genre"), "test")
        client.get("/songs").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue { bodyAsText().contains("test") }
        }
    }

    @Test
    fun testGetSongsEmpty() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.get("/songs").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}