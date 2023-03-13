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
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.service.AppService

class AutoMediaBrowserService : MediaBrowserServiceCompat(), ServiceConnection {

    override fun onCreate() {
        super.onCreate()

        if (App.service != null) {
            setSessionToken()
        } else {
            val intent = Intent(applicationContext, AppService::class.java)
            applicationContext.bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("media_root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = Station.values().map {
            createPlayableMediaItem(it.name, resources.getString(it.labelRes))
        }

        result.sendResult(mediaItems)
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as AppService.ServiceBinder
        val radioService = binder.service

        App.service = radioService
        setSessionToken()
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
    }

    private fun setSessionToken() {
        App.service?.mediaSession?.let {
            sessionToken = it.sessionToken
        }
    }

    private fun createPlayableMediaItem(mediaId: String, title: String): MediaBrowserCompat.MediaItem {
        val builder = MediaDescriptionCompat.Builder()
            .setMediaId(mediaId)
            .setTitle(title)

        return MediaBrowserCompat.MediaItem(builder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
}
