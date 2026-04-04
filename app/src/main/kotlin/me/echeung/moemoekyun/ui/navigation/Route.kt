package me.echeung.moemoekyun.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.domain.songs.model.DomainSong

sealed interface Route : NavKey {
    @Serializable data object Home : Route

    @Serializable data object Music : Route

    @Serializable data object Settings : Route

    @Serializable data object About : Route

    @Serializable data object Licenses : Route

    @Serializable data object Login : Route

    @Serializable data object Register : Route

    @Serializable data class Songs(val songs: ImmutableList<DomainSong>, val moreUrl: String? = null) : Route
}
