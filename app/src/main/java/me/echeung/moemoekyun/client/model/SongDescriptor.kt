package me.echeung.moemoekyun.client.model

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
        fun getDisplayString(songDescriptors: List<SongDescriptor>?, preferRomaji: Boolean): String? {
            if (songDescriptors == null) {
                return null
            }

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
