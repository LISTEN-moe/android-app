package me.echeung.moemoekyun.widget

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun favoriteSong(): FavoriteSong
    fun radioWidgetUpdater(): RadioWidgetUpdater
}

fun widgetEntryPoint(context: Context): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
