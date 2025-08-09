package me.echeung.moemoekyun.domain.songs.model

import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongDescriptor
import me.echeung.moemoekyun.util.PreferenceUtil
import java.util.Locale
import javax.inject.Inject

class SongConverter @Inject constructor(private val preferenceUtil: PreferenceUtil) {

    fun toDomainSong(song: Song): DomainSong = DomainSong(
        id = song.id,
        title = if (preferenceUtil.shouldPreferRomaji().get()) {
            (song.titleRomaji ?: song.title).orEmpty().trim()
        } else {
            song.title.orEmpty().trim()
        },
        artists = song.artists?.toDomainSong(),
        albums = song.albums?.toDomainSong(),
        sources = song.sources?.toDomainSong(),
        duration = song.duration(),
        durationSeconds = song.duration.toLong(),
        albumArtUrl = song.albumArtUrl(),
        favorited = song.favorite,
        favoritedAtEpoch = song.favoritedAt,
    )

    private fun List<SongDescriptor>.toDomainSong(): String = this
        .mapNotNull {
            val preferredName = if (preferenceUtil.shouldPreferRomaji().get()) {
                it.nameRomaji
            } else {
                it.name
            }
            preferredName ?: it.name
        }
        .joinToString()

    private fun Song.duration(): String {
        var minutes = (duration / 60).toLong()
        val seconds = (duration % 60).toLong()
        return if (minutes < 60) {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        } else {
            val hours = minutes / 60
            minutes %= 60
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    private fun Song.albumArtUrl(): String? {
        val album = albums?.firstOrNull { it.image != null }
        if (album != null) {
            return "$CDN_ALBUM_ART_URL/${album.image}"
        }

        return null
    }
}

private const val CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers"
