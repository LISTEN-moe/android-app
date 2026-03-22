package me.echeung.moemoekyun.service

data class VisualizerState(val magnitudes: FloatArray) {
    companion object {
        const val BAND_COUNT = 32
        val EMPTY = VisualizerState(FloatArray(BAND_COUNT))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisualizerState) return false
        return magnitudes.contentEquals(other.magnitudes)
    }

    override fun hashCode(): Int = magnitudes.contentHashCode()
}
