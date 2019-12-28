package me.echeung.moemoekyun.util

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongDetailAdapter
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.IsFavoriteCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SongCallback
import me.echeung.moemoekyun.client.auth.AuthTokenUtil
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.util.ext.clipboardManager
import me.echeung.moemoekyun.util.ext.toast
import me.echeung.moemoekyun.viewmodel.RadioViewModel

class SongActionsUtil(
        private val radioClient: RadioClient,
        private val preferenceUtil: PreferenceUtil,
        private val authTokenUtil: AuthTokenUtil,
        private val radioViewModel: RadioViewModel
) {

    fun showSongsDialog(activity: Activity, title: String?, song: Song) {
        showSongsDialog(activity, title, listOf(song))
    }

    fun showSongsDialog(activity: Activity?, title: String?, songs: List<Song>) {
        if (activity == null) return

        val detailedSongs = songs.toMutableList()
        val adapter = SongDetailAdapter(activity, detailedSongs)

        // Asynchronously update songs with more details
        detailedSongs.forEachIndexed { index, song ->
            radioClient.api.getSongDetails(song.id, object : SongCallback {
                override fun onSuccess(detailedSong: Song) {
                    detailedSong.favorite = song.favorite
                    detailedSongs[index] = detailedSong

                    activity.runOnUiThread {
                        adapter.notifyDataSetInvalidated()
                    }
                }
            })
        }

        // Get favorited status if logged in
        if (authTokenUtil.isAuthenticated) {
            val songIds = songs.map { it.id }
            radioClient.api.isFavorite(songIds, object : IsFavoriteCallback {
                override fun onSuccess(favoritedSongIds: List<Int>) {
                    detailedSongs.forEach {
                        if (it.id in favoritedSongIds) {
                            it.favorite = true
                        }
                    }

                    activity.runOnUiThread {
                        adapter.notifyDataSetInvalidated()
                    }
                }
            })
        }

        AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(title)
                .setAdapter(adapter, null)
                .create()
                .show()
    }

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    fun toggleFavorite(activity: Activity?, song: Song) {
        val songId = song.id
        val isCurrentlyFavorite = song.favorite

        radioClient.api.toggleFavorite(songId, object : FavoriteSongCallback {
            override fun onSuccess() {
                if (radioViewModel.currentSong!!.id == songId) {
                    radioViewModel.isFavorited = !isCurrentlyFavorite
                }
                song.favorite = !isCurrentlyFavorite

                if (activity == null) return

                activity.runOnUiThread {
                    // Broadcast event
                    activity.sendBroadcast(Intent(FAVORITE_EVENT))

                    if (isCurrentlyFavorite) {
                        // Undo action
                        val coordinatorLayout = activity.findViewById<View>(R.id.coordinator_layout)
                        if (coordinatorLayout != null) {
                            val undoBar = Snackbar.make(coordinatorLayout,
                                    String.format(activity.getString(R.string.unfavorited), song.toString()),
                                    Snackbar.LENGTH_LONG)
                            undoBar.setAction(R.string.action_undo) { toggleFavorite(activity, song) }
                            undoBar.show()
                        }
                    }
                }
            }

            override fun onFailure(message: String?) {
                if (activity == null) return

                activity.runOnUiThread { activity.applicationContext.toast(message) }
            }
        })
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    fun request(activity: Activity?, song: Song) {
        radioClient.api.requestSong(song.id, object : RequestSongCallback {
            override fun onSuccess() {
                if (activity == null) return

                activity.runOnUiThread {
                    // Broadcast event
                    activity.sendBroadcast(Intent(REQUEST_EVENT))

                    val toastMsg = if (preferenceUtil.shouldShowRandomRequestTitle())
                        activity.getString(R.string.requested_song, song.toString())
                    else
                        activity.getString(R.string.requested_random_song)

                    activity.applicationContext.toast(toastMsg, Toast.LENGTH_LONG)
                }
            }

            override fun onFailure(message: String?) {
                if (activity == null) return

                activity.runOnUiThread { activity.applicationContext.toast(message) }
            }
        })
    }

    fun copyToClipboard(context: Context?, song: Song?) {
        if (context == null || song == null) return

        copyToClipboard(context, song.toString())
    }

    private fun copyToClipboard(context: Context, songInfo: String) {
        val clip = ClipData.newPlainText("song", songInfo)
        context.clipboardManager.setPrimaryClip(clip)

        context.toast("${context.getString(R.string.copied_to_clipboard)}: $songInfo")
    }

    companion object {
        const val REQUEST_EVENT = "req_event"
        const val FAVORITE_EVENT = "fav_event"
    }
}
