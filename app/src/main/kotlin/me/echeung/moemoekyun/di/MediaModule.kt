package me.echeung.moemoekyun.di

import android.content.Context
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import me.echeung.moemoekyun.util.ext.audioManager
import me.echeung.moemoekyun.util.system.NetworkUtil

@Module
@OptIn(UnstableApi::class)
@InstallIn(ServiceComponent::class)
object MediaModule {

    @Provides
    fun audioManager(@ApplicationContext context: Context): AudioManager = context.audioManager

    @Provides
    @Reusable
    fun audioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Reusable
    fun liveConfiguration() = MediaItem.LiveConfiguration.Builder()
        .setTargetOffsetMs(0)
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    fun dateSourceFactory(@ApplicationContext context: Context): DefaultDataSource.Factory = DefaultDataSource.Factory(
        context,
        DefaultHttpDataSource.Factory()
            .setUserAgent(NetworkUtil.userAgent),
    )

    @OptIn(UnstableApi::class)
    @Provides
    fun progressiveMediaSourceFactory(dataSourceFactory: DefaultDataSource.Factory): ProgressiveMediaSource.Factory =
        ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())

    @Provides
    fun exoPlayer(
        @ApplicationContext context: Context,
        progressiveMediaSourceFactory: ProgressiveMediaSource.Factory,
        audioAttributes: AudioAttributes,
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(progressiveMediaSourceFactory)
        .setAudioAttributes(audioAttributes, true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()
}
