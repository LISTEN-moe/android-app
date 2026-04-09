package me.echeung.moemoekyun.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.echeung.moemoekyun.service.PlaybackService
import me.echeung.moemoekyun.ui.MainActivity
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val songIdParam = ActionParameters.Key<Int>("song_id")

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withMediaController(context) { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
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

/**
 * Connects a [MediaController] to [PlaybackService], calls [block], then immediately releases
 * the controller so the service binding is not held longer than needed.
 *
 * [MediaController.Builder.buildAsync] and all MediaController interactions must run on the main
 * thread, hence the explicit [Dispatchers.Main] context.
 */
private suspend fun withMediaController(context: Context, block: (MediaController) -> Unit) {
    withContext(Dispatchers.Main) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        val controller = suspendCancellableCoroutine { cont ->
            future.addListener(
                {
                    runCatching { future.get() }
                        .onSuccess { cont.resume(it) }
                        .onFailure { cont.resumeWithException(it) }
                },
                context.mainExecutor,
            )
            cont.invokeOnCancellation { MediaController.releaseFuture(future) }
        }
        try {
            block(controller)
        } finally {
            MediaController.releaseFuture(future)
        }
    }
}
