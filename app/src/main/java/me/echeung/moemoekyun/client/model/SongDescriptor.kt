package me.echeung.moemoekyun.client.model

import me.echeung.moemoekyun.App

data class SongDescriptor(
    val name: String? = null,
    val nameRomaji: String? = null,
    val image: String? = null
) {

    fun contains(query: String): Boolean {
        return name.orEmpty().contains(query, ignoreCase = true) ||
                nameRomaji.orEmpty().contains(query, ignoreCase = true)
    }

    companion object {
        fun getDisplayString(songDescriptors: List<SongDescriptor>?): String? {
            if (songDescriptors == null) {
                return null
            }

            val preferRomaji = App.preferenceUtil!!.shouldPreferRomaji()

            val displayString = songDescriptors
                    .mapNotNull {
                        if (preferRomaji && !it.nameRomaji.isNullOrBlank()) {
                            it.nameRomaji
                        } else {
                            it.name
                        }
                    }
                    .joinToString(", ")

            return if (displayString.isNotEmpty()) displayString else null
        }
    }
}
