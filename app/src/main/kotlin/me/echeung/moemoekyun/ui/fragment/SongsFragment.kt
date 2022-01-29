package me.echeung.moemoekyun.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsListAdapter
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.databinding.FragmentSongsBinding
import me.echeung.moemoekyun.ui.base.SongsListBaseFragment
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchUI
import me.echeung.moemoekyun.util.ext.toast
import org.koin.android.ext.android.inject

class SongsFragment : SongsListBaseFragment<FragmentSongsBinding>() {

    private val radioClient: RadioClient by inject()
    private val songSortUtil: SongSortUtil by inject()

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
        songSortUtil.initSortMenu(LIST_ID, menu)

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
            binding.searchView,
            LIST_ID,
            this::loadSongs
        )
    }

    private fun loadSongs(adapter: SongsListAdapter) {
        songList.showLoading(true)

        launchIO {
            try {
                val favorites = radioClient.api.search(null)
                launchUI { adapter.songs = favorites }
            } catch (e: Exception) {
                launchUI { activity?.toast(e.message) }
            } finally {
                launchUI { songList.showLoading(false) }
            }
        }
    }
}

private const val LIST_ID = "SONGS_LIST"
