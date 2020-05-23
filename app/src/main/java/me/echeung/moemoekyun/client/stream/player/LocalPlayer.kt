package me.echeung.moemoekyun.client.stream.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.util.system.NetworkUtil

class LocalPlayer(private val context: Context) : MusicPlayer<SimpleExoPlayer>() {

    override fun initPlayer() {
        if (player == null) {
            player = SimpleExoPlayer.Builder(context).build()

            player!!.setWakeMode(C.WAKE_MODE_NETWORK)

            player!!.addListener(eventListener)
            player!!.volume = 1f

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            player!!.audioAttributes = audioAttributes
        }

        // Set stream
        val streamUrl = RadioClient.library!!.streamUrl
        if (streamUrl != currentStreamUrl) {
            val dataSourceFactory = DefaultDataSourceFactory(context, NetworkUtil.userAgent)
            val streamSource = ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(streamUrl))

            player!!.prepare(streamSource)
            currentStreamUrl = streamUrl
        }
    }
}
