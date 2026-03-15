package me.echeung.moemoekyun.di

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

/**
 * Player binding module for the F-Droid variant.
 * Binds Player directly to ExoPlayer without cast support.
 */
@Module
@OptIn(UnstableApi::class)
@InstallIn(ServiceComponent::class)
object PlayerModule {

    @Provides
    fun player(exoPlayer: ExoPlayer): Player = exoPlayer
}

