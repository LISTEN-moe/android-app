package me.echeung.moemoekyun.service.auto

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.service.RadioService

class AutoMediaBrowserService : MediaBrowserServiceCompat(), ServiceConnection {

    override fun onCreate() {
        super.onCreate()

        if (App.service != null) {
            setSessionToken()
        } else {
            val intent = Intent(applicationContext, RadioService::class.java)
            applicationContext.bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MEDIA_ID_ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = listOf(
            createPlayableMediaItem(RadioService.LIBRARY_JPOP, resources.getString(R.string.jpop)),
            createPlayableMediaItem(RadioService.LIBRARY_KPOP, resources.getString(R.string.kpop))
        )

        result.sendResult(mediaItems)
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as RadioService.ServiceBinder
        val radioService = binder.service

        App.service = radioService
        setSessionToken()
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
    }

    private fun setSessionToken() {
        val mediaSession = App.service!!.mediaSession
        sessionToken = mediaSession!!.sessionToken
    }

    private fun createPlayableMediaItem(mediaId: String, title: String): MediaBrowserCompat.MediaItem {
        val builder = MediaDescriptionCompat.Builder()
            .setMediaId(mediaId)
            .setTitle(title)

        return MediaBrowserCompat.MediaItem(builder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
}

private const val MEDIA_ID_ROOT = "media_root"
