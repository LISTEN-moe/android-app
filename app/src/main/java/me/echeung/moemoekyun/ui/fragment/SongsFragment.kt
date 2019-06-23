package me.echeung.moemoekyun.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsAdapter
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.databinding.FragmentSongsBinding
import me.echeung.moemoekyun.ui.activity.MainActivity
import me.echeung.moemoekyun.ui.base.SongsListBaseFragment
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.system.toast

class SongsFragment : SongsListBaseFragment<FragmentSongsBinding>(), SongList.SongListLoader, SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        layout = R.layout.fragment_songs

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action != null) {
                    when (action) {
                        MainActivity.AUTH_EVENT -> songList.loadSongs()
                        SongActionsUtil.FAVORITE_EVENT -> songList.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun initSongList(binding: FragmentSongsBinding): SongList {
        binding.songsList.vm = songListVm

        return SongList(
                requireActivity(),
                songListVm,
                binding.songsList.list,
                binding.songsList.refreshLayout,
                binding.songsList.query,
                binding.songsList.overflowBtn,
                "SONGS_LIST",
                this)
    }

    override fun loadSongs(adapter: SongsAdapter) {
        songList.showLoading(true)

        App.radioClient!!.api.search(null, object : SearchCallback {
            override fun onSuccess(favorites: List<Song>) {
                activity?.runOnUiThread {
                    songList.showLoading(false)
                    adapter.songs = favorites
                }
            }

            override fun onFailure(message: String?) {
                activity?.runOnUiThread {
                    songList.showLoading(false)
                    activity?.toast(message)
                }
            }
        })
    }
}
