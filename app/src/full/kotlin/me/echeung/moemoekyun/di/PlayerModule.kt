package me.echeung.moemoekyun.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.RemoteCastPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import me.echeung.moemoekyun.service.PlaybackMediaItemConverter

/**
 * Cast-specific module for the full variant.
 * Provides cast player with remote casting support.
 */
@Module
@OptIn(UnstableApi::class)
@InstallIn(ServiceComponent::class)
object PlayerModule {

    @Provides
    fun defaultMediaItemConverter(): DefaultMediaItemConverter = DefaultMediaItemConverter()

    @Provides
    fun remotePlayer(
        @ApplicationContext context: Context,
        mediaItemConverter: PlaybackMediaItemConverter,
    ): RemoteCastPlayer = RemoteCastPlayer.Builder(context)
        .setMediaItemConverter(mediaItemConverter)
        .build()

    @Provides
    fun castPlayer(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer,
        remotePlayer: RemoteCastPlayer,
    ): Player = CastPlayer.Builder(context)
        .setLocalPlayer(exoPlayer)
        .setRemotePlayer(remotePlayer)
        .build()
}

