package me.echeung.moemoekyun.client.model

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    var id: Int = 0,
    var title: String? = null,
    var titleRomaji: String? = null,
    var artists: List<SongDescriptor>? = null,
    var sources: List<SongDescriptor>? = null,
    var albums: List<SongDescriptor>? = null,
    var duration: Int = 0,
    var enabled: Boolean = false,
    var favorite: Boolean = false,
    var favoritedAt: Long? = null,
)
