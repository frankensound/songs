package com.frankensound.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Songs : IntIdTable("songs", "id") {
    val key: Column<String> = varchar("key", 255).uniqueIndex()
    val userId: Column<String> = varchar("user_id", 255)
}

class Song(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Song>(Songs)

    var key by Songs.key
    var userId by Songs.userId
}

object Details : IntIdTable("details", "id") {
    val artistName: Column<String> = varchar("artist_name", 255)
    val songTitle: Column<String> = varchar("song_title", 255)
    val genre: Column<String> = varchar("genre", 255)
    val song = reference("song_id", Songs, onDelete = ReferenceOption.CASCADE)
}

class Detail(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Detail>(Details)

    var artistName by Details.artistName
    var songTitle by Details.songTitle
    var genre by Details.genre
    var song by Song referencedOn Details.song
}

@Serializable
data class SongData(
    val key: String,
    val userId: String
) {
    companion object {
        fun Song.serialized() = transaction {
            SongData(
                key,
                userId
            )
        }
    }
}

@Serializable
data class DetailData(
    val artistName: String,
    val songTitle: String,
    val genre: String
) {
    companion object {
        fun Detail.serialized() = transaction {
            DetailData(
                artistName,
                songTitle,
                genre
            )
        }
    }
}

@Serializable
data class RequestDTO(
    val key: String,
    @SerialName("detail")
    val detailDto: DetailDTO
) {
    @Serializable
    data class DetailDTO(
        @SerialName("artist_name")
        val artistName: String,
        @SerialName("song_title")
        val songTitle: String,
        @SerialName("genre")
        val genre: String
    )
}