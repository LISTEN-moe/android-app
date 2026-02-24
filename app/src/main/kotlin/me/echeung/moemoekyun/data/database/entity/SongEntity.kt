package me.echeung.moemoekyun.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.echeung.moemoekyun.domain.songs.model.DomainSong

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val artists: String?,
    val albums: String?,
    val sources: String?,
    val duration: String,
    val durationSeconds: Long,
    val albumArtUrl: String?,
)

fun DomainSong.toSongEntity() = SongEntity(
    id = id,
    title = title,
    artists = artists,
    albums = albums,
    sources = sources,
    duration = duration,
    durationSeconds = durationSeconds,
    albumArtUrl = albumArtUrl,
)
