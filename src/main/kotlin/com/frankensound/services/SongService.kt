package com.frankensound.services

import com.frankensound.models.*
import com.frankensound.models.SongData.Companion.serialized
import com.frankensound.utils.EventBus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class SongService {
    init {
        EventBus.subscribe { jsonElement ->
            GlobalScope.launch {
                try {
                    // Assuming the message contains a userId field
                    val userId = jsonElement.jsonObject["userId"]?.jsonPrimitive?.content
                    if (userId != null) {
                        deleteSongsForUser(userId)
                    }
                } catch (e: Exception) {
                    println("Error handling message: ${e.message}")
                }
            }
        }
    }

    // Fetches all songs from the database
    suspend fun getAll() = transaction {
        Song.all().map { it.serialized() }
    }

    // Fetches a single song by id
    suspend fun get(id: Int): Song? = transaction {
        Song.find { Songs.id eq id }.singleOrNull()
    }

    // Fetches a single song by key
    suspend fun get(key: String): Song? = transaction {
        Song.find { Songs.key eq key }.singleOrNull()
    }

    // Creates a new song in the database
    suspend fun create(key: String, detail: DetailData, userId: String): Song {
        val query = transaction {
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
    private suspend fun insertDetail(song: Song, detail: DetailData): Detail = transaction {
        Detail.new {
            this.song = song
            this.artistName = detail.artistName
            this.songTitle = detail.songTitle
            this.genre = detail.genre
        }
    }

    // Fetches detail by id
    private suspend fun getDetailById(id: Int): Detail? = transaction {
        Detail.find { Details.id eq id }.singleOrNull()
    }

    // Validates the song key
    private fun validateSongKey(key: String) {
        if (key.isBlank()) {
            throw IllegalArgumentException("Song key cannot be blank")
        }
    }

    suspend fun deleteSongsForUser(userId: String): Boolean = newSuspendedTransaction {
        val deletedCount = Songs.deleteWhere { Songs.userId eq userId }
        deletedCount > 0
    }
}