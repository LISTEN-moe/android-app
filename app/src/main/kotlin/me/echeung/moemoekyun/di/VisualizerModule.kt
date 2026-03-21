package me.echeung.moemoekyun.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.echeung.moemoekyun.service.VisualizerAudioProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VisualizerModule {

    /**
     * Provided at SingletonComponent scope (not ServiceComponent) so the same instance
     * is accessible from both the playback service and the UI layer (HomeScreenModel).
     */
    @Provides
    @Singleton
    fun visualizerAudioProcessor(): VisualizerAudioProcessor = VisualizerAudioProcessor()
}
