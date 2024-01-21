package com.frankensound.services

import com.frankensound.models.*
import com.frankensound.models.SongData.Companion.serialized
import com.frankensound.utils.EventBus
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class SongService {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    init {
        EventBus.subscribe { jsonElement ->
            serviceScope.launch {
                handleEvent(jsonElement)
            }
        }
    }

    private suspend fun handleEvent(jsonElement: JsonElement) {
        try {
            val userId = jsonElement.jsonObject["userId"]?.jsonPrimitive?.content
            userId?.let { deleteSongsForUser(it) }
        } catch (e: Exception) {
            println("Error handling message: ${e.message}")
        }
    }

    // Fetches all songs from the database
    suspend fun getAll() = newSuspendedTransaction {
        Song.all().map { it.serialized() }
    }

    // Fetches a single song by id
    suspend fun get(id: Int): Song? = newSuspendedTransaction {
        Song.find { Songs.id eq id }.singleOrNull()
    }

    // Fetches a single song by key
    suspend fun get(key: String): Song? = newSuspendedTransaction {
        Song.find { Songs.key eq key }.singleOrNull()
    }

    // Fetches a single song and its details by id
    suspend fun getWithDetails(id: Int): Pair<Song?, Detail?> = newSuspendedTransaction {
        val song = Song.findById(id)
        val detail = song?.let { Detail.find { Details.song eq it.id }.singleOrNull() }
        song to detail
    }

    // Creates a new song in the database
    suspend fun create(key: String, detail: DetailData, userId: String): Song {
        val query = newSuspendedTransaction {
            validateSongKey(key)

            Song.new {
                this.key = key
                this.userId = userId
            }
        }

        newSuspendedTransaction {
            insertDetail(query, detail)
        }

        return query
    }

    // Updates an existing song
    suspend fun update(id: Int, detail: DetailData) = newSuspendedTransaction {
        val song = get(id)
        if(song == null){
            false
        }
        else{
            val d = insertDetail(song, detail)
            true
        }
    }

    // Deletes a song by id
    suspend fun delete(id: Int): Boolean = newSuspendedTransaction {
        val query = get(id)
        if(query == null){
            false
        }
        else {
            query.delete()
            true
        }
    }

    // Inserts song detail and returns the generated id
    suspend fun insertDetail(song: Song, detail: DetailData): Detail = newSuspendedTransaction {
        Detail.new {
            this.song = song
            this.artistName = detail.artistName
            this.songTitle = detail.songTitle
            this.genre = detail.genre
        }
    }

    // Fetches detail by id
    suspend fun getDetailById(id: Int): Detail? = newSuspendedTransaction {
        Detail.find { Details.id eq id }.singleOrNull()
    }

    // Validates the song key
    fun validateSongKey(key: String) {
        if (key.isBlank()) {
            throw IllegalArgumentException("Song key cannot be blank")
        }
    }

    suspend fun deleteSongsForUser(userId: String): Boolean = newSuspendedTransaction {
        val deletedCount = Songs.deleteWhere { Songs.userId eq userId }
        deletedCount > 0
    }

    fun close() {
        serviceScope.cancel()
    }
}