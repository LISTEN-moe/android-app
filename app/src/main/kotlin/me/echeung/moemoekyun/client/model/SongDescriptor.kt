package me.echeung.moemoekyun.client.model

import kotlinx.serialization.Serializable

@Serializable
data class SongDescriptor(val name: String? = null, val nameRomaji: String? = null, val image: String? = null) {

    fun contains(query: String): Boolean = name.orEmpty().contains(query, ignoreCase = true) ||
        nameRomaji.orEmpty().contains(query, ignoreCase = true)
}
