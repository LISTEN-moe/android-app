package me.echeung.moemoekyun.di

import android.content.Context
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.echeung.moemoekyun.util.ext.audioManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun audioManager(@ApplicationContext context: Context): AudioManager = context.audioManager

    @Provides
    @Singleton
    fun audioAttributesCompat(): AudioAttributesCompat = AudioAttributesCompat.Builder()
        .setUsage(AudioAttributesCompat.USAGE_MEDIA)
        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
        .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
        .build()

    @Provides
    @Singleton
    fun audioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
}
