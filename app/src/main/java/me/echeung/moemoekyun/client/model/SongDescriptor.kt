package me.echeung.moemoekyun.client.model

import me.echeung.moemoekyun.App

data class SongDescriptor(
        val id: Int = 0,
        val name: String? = null,
        val nameRomaji: String? = null,
        val image: String? = null) {

    fun contains(query: String): Boolean {
        return name.orEmpty().contains(query, ignoreCase = true)
                || nameRomaji.orEmpty().contains(query, ignoreCase = true)
    }

    companion object {
        fun getDisplayString(songDescriptors: List<SongDescriptor>?): String? {
            if (songDescriptors == null) {
                return null
            }

            val preferRomaji = App.preferenceUtil!!.shouldPreferRomaji()

            val s = StringBuilder()
            for (songDescriptor in songDescriptors) {
                if (songDescriptor.name == null) {
                    continue
                }

                if (s.isNotEmpty()) {
                    s.append(", ")
                }

                if (preferRomaji && !songDescriptor.nameRomaji.isNullOrBlank()) {
                    s.append(songDescriptor.nameRomaji)
                } else {
                    s.append(songDescriptor.name)
                }
            }
            return s.toString()
        }
    }
}
