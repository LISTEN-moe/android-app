package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSessionService
import logcat.logcat
import javax.inject.Inject

@OptIn(UnstableApi::class)
class PlaybackServiceSessionListener @Inject constructor(): MediaSessionService.Listener {
    override fun onForegroundServiceStartNotAllowedException() {
        logcat { "Couldn't start foreground service." }
        super.onForegroundServiceStartNotAllowedException()
    }
}
