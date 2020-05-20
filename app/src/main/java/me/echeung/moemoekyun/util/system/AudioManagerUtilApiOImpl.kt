package me.echeung.moemoekyun.util.system

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class AudioManagerUtilApiOImpl(
    context: Context,
    audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
) : AudioManagerUtil {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val focusRequest: AudioFocusRequest

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
    }

    override fun requestAudioFocus(): Int {
        return audioManager.requestAudioFocus(focusRequest)
    }

    override fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}
