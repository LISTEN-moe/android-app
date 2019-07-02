package me.echeung.moemoekyun.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsAdapter
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.databinding.FragmentSongsBinding
import me.echeung.moemoekyun.ui.base.SongsListBaseFragment
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.system.toast

class SongsFragment : SongsListBaseFragment<FragmentSongsBinding>(), SongList.SongListLoader, SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        layout = R.layout.fragment_songs

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action != null) {
                    when (action) {
                        SongActionsUtil.FAVORITE_EVENT -> songList.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        binding.songListVm = songListVm

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sort, menu)
        SongSortUtil.initSortMenu(requireContext(), LIST_ID, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return songList.handleMenuItemClick(item)
    }

    override fun initSongList(binding: FragmentSongsBinding): SongList {
        return SongList(
                requireActivity(),
                songListVm,
                binding.songs.list,
                binding.songs.refreshLayout,
//                binding.songs.filter.query,
                null,
                null,
                LIST_ID,
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

    companion object {
        private const val LIST_ID = "SONGS_LIST"
    }
}
