package com.frankensound.api.songs

import com.frankensound.models.DetailData
import com.frankensound.routes.songService
import com.frankensound.utils.TestDatabaseFactory
import com.frankensound.utils.configureTestEnvironment
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class GetTests {
    @BeforeTest
    fun setup() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }
    @Test
    fun testGetSongByKey() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        val key = "test"
        songService.create(key, DetailData("artist","title"))
        client.get("/songs/$key").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue { bodyAsText().contains("song") }
        }
    }

    @Test
    fun testGetSongByNonexistentKey() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.get("/songs/nonexistentkey").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testGetSongMissingKey() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.get("/songs/").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}