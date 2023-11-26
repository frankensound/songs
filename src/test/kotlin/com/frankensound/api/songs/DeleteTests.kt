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
import kotlin.test.*

class DeleteTests {
    @BeforeTest
    fun setup() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }
    @Test
    fun testDeleteSong() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        val song = songService.create("test", DetailData("artist","title"))
        val key = song.key
        client.delete("/songs/$key").apply {
            assertEquals(HttpStatusCode.Accepted, status)
        }
    }

    @Test
    fun testDeleteNonexistentSong() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        val key = "key123"
        client.delete("/songs/$key").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testDeleteSongMissingKey() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }
        application {
            configureTestEnvironment()
        }
        client.delete("/songs/}").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}