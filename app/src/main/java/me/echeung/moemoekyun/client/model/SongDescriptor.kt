package me.echeung.moemoekyun.client.model

import android.text.TextUtils
import me.echeung.moemoekyun.App

class SongDescriptor {
    var id: Int = 0
    var name: String? = null
    var nameRomaji: String? = null
    var image: String? = null

    fun contains(query: String): Boolean {
        return name.orEmpty().contains(query, ignoreCase = true)
                || nameRomaji.orEmpty().contains(query, ignoreCase = true)
    }

    companion object {
        fun getSongDescriptorsString(songDescriptors: List<SongDescriptor>?): String {
            if (songDescriptors == null) {
                return ""
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

                if (preferRomaji && !TextUtils.isEmpty(songDescriptor.nameRomaji)) {
                    s.append(songDescriptor.nameRomaji)
                } else {
                    s.append(songDescriptor.name)
                }
            }
            return s.toString()
        }
    }
}
