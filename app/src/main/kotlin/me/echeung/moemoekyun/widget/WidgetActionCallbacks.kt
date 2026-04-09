package me.echeung.moemoekyun.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import me.echeung.moemoekyun.service.ACTION_TOGGLE_PLAYBACK
import me.echeung.moemoekyun.service.PlaybackService
import me.echeung.moemoekyun.ui.MainActivity

val songIdParam = ActionParameters.Key<Int>("song_id")

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        context.startService(
            Intent(context, PlaybackService::class.java).apply {
                action = ACTION_TOGGLE_PLAYBACK
            },
        )
    }
}

class FavoriteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val songId = parameters[songIdParam] ?: return
        widgetEntryPoint(context).favoriteSong().await(songId)
        // Trigger an immediate re-render; the RadioService flow will emit the authoritative state
        // once the server acknowledges the favourite toggle via the socket.
        widgetEntryPoint(context).radioWidgetUpdater().requestUpdate()
    }
}

class OpenAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
        )
    }
}
