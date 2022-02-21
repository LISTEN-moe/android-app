package me.echeung.moemoekyun.util

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongDetailAdapter
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.auth.AuthUtil
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.util.ext.clipboardManager
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchUI
import me.echeung.moemoekyun.util.ext.toast
import me.echeung.moemoekyun.viewmodel.RadioViewModel

class SongActionsUtil(
    private val radioClient: RadioClient,
    private val preferenceUtil: PreferenceUtil,
    private val authUtil: AuthUtil,
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
        launchIO {
            detailedSongs.forEachIndexed { index, song ->
                val detailedSong = radioClient.api.getSongDetails(song.id)
                detailedSong.favorite = song.favorite
                detailedSongs[index] = detailedSong

                launchUI {
                    adapter.notifyDataSetInvalidated()
                }
            }
        }

        // Get favorited status if logged in
        if (authUtil.isAuthenticated) {
            val songIds = songs.map { it.id }
            launchIO {
                val favoritedSongIds = radioClient.api.isFavorite(songIds)

                detailedSongs
                    .filter { it.id in favoritedSongIds }
                    .forEach { it.favorite = true }

                launchUI {
                    adapter.notifyDataSetInvalidated()
                }
            }
        }

        MaterialAlertDialogBuilder(activity)
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

        launchIO {
            try {
                radioClient.api.toggleFavorite(songId)

                if (radioViewModel.currentSong!!.id == songId) {
                    radioViewModel.isFavorited = !isCurrentlyFavorite
                }
                song.favorite = !isCurrentlyFavorite

                launchUI {
                    activity ?: return@launchUI

                    // Broadcast event
                    activity.sendBroadcast(Intent(FAVORITE_EVENT).apply {
                        setPackage(activity.packageName)
                    })

                    if (isCurrentlyFavorite) {
                        // Undo action
                        val coordinatorLayout = activity.findViewById<View>(R.id.coordinator_layout)
                        if (coordinatorLayout != null) {
                            val undoBar = Snackbar.make(
                                coordinatorLayout,
                                String.format(activity.getString(R.string.unfavorited), song.toString()),
                                Snackbar.LENGTH_LONG
                            )
                            undoBar.setAction(R.string.action_undo) { toggleFavorite(activity, song) }
                            undoBar.show()
                        }
                    }
                }
            } catch (e: Exception) {
                launchUI { activity?.toast(e.message) }
            }
        }
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    fun request(activity: Activity?, song: Song) {
        launchIO {
            try {
                radioClient.api.requestSong(song.id)

                launchUI {
                    activity ?: return@launchUI

                    // Broadcast event
                    activity.sendBroadcast(Intent(REQUEST_EVENT))

                    val toastMsg = if (preferenceUtil.shouldShowRandomRequestTitle()) {
                        activity.getString(R.string.requested_song, song.toString())
                    } else {
                        activity.getString(R.string.requested_random_song)
                    }

                    activity.toast(toastMsg, Toast.LENGTH_LONG)
                }
            } catch (e: Exception) {
                launchUI { activity?.toast(e.message) }
            }
        }
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
