package me.echeung.moemoekyun.util.system

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioManager
import android.os.Build

@TargetApi(Build.VERSION_CODES.N_MR1)
class AudioManagerUtilLegacyApiImpl(
    context: Context,
    private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
) : AudioManagerUtil {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun requestAudioFocus(): Int {
        return audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
}
