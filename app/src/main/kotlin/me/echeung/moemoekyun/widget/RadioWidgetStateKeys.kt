package me.echeung.moemoekyun.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal val keyStation = stringPreferencesKey("station")
internal val keyTitle = stringPreferencesKey("title")
internal val keyArtists = stringPreferencesKey("artists")
internal val keyAlbumArtUrl = stringPreferencesKey("album_art_url")
internal val keyIsPlaying = booleanPreferencesKey("is_playing")
internal val keyIsFavorited = booleanPreferencesKey("is_favorited")
internal val keyIsAuthenticated = booleanPreferencesKey("is_authenticated")
internal val keySongId = intPreferencesKey("song_id")
