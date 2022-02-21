package me.echeung.moemoekyun.client.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SongDescriptor(
    val name: String? = null,
    val nameRomaji: String? = null,
    val image: String? = null,
) {

    fun contains(query: String): Boolean {
        return name.orEmpty().contains(query, ignoreCase = true) ||
            nameRomaji.orEmpty().contains(query, ignoreCase = true)
    }
}

fun List<SongDescriptor>?.getSongDisplayString(preferRomaji: Boolean): String? {
    if (isNullOrEmpty()) {
        return null
    }

    return mapNotNull {
            if (preferRomaji && !it.nameRomaji.isNullOrBlank()) {
                it.nameRomaji
            } else {
                it.name
            }
        }
        .joinToString(", ")
}
