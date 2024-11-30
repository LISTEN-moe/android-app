package me.echeung.moemoekyun.util.system

import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class AudioManagerUtil @AssistedInject constructor(
    private val audioManager: AudioManager,
    audioAttributes: AudioAttributesCompat,
    @Assisted audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener,
) {

    private val focusRequest: AudioFocusRequestCompat = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
        .setAudioAttributes(audioAttributes)
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(audioFocusChangeListener)
        .build()

    fun requestAudioFocus(): Int {
        return AudioManagerCompat.requestAudioFocus(audioManager, focusRequest)
    }

    fun abandonAudioFocus() {
        AudioManagerCompat.abandonAudioFocusRequest(audioManager, focusRequest)
    }

    @AssistedFactory
    interface Factory {
        fun create(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener): AudioManagerUtil
    }
}
