package me.echeung.moemoekyun.util.system

import android.content.Context
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import me.echeung.moemoekyun.util.ext.audioManager

class AudioManagerUtil(
    context: Context,
    audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
) {

    private val audioManager: AudioManager = context.audioManager
    private val focusRequest: AudioFocusRequestCompat

    init {
        val audioAttributes = AudioAttributesCompat.Builder()
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
    }

    fun requestAudioFocus(): Int {
        return AudioManagerCompat.requestAudioFocus(audioManager, focusRequest)
    }

    fun abandonAudioFocus() {
        AudioManagerCompat.abandonAudioFocusRequest(audioManager, focusRequest)
    }
}
