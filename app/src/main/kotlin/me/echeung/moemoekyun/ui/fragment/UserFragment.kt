package me.echeung.moemoekyun.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsListAdapter
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.client.api.auth.AuthUtil
import me.echeung.moemoekyun.databinding.FragmentUserBinding
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil
import me.echeung.moemoekyun.ui.base.SongsListBaseFragment
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchUI
import me.echeung.moemoekyun.util.ext.popupMenu
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.android.inject

class UserFragment : SongsListBaseFragment<FragmentUserBinding>() {

    private val radioClient: RadioClient by inject()
    private val authUtil: AuthUtil by inject()
    private val songSortUtil: SongSortUtil by inject()

    private val radioViewModel: RadioViewModel by inject()
    private val userViewModel: UserViewModel by inject()

    init {
        layout = R.layout.fragment_user

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    AuthActivityUtil.AUTH_EVENT, SongActionsUtil.FAVORITE_EVENT -> initUserContent()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        binding.radioVm = radioViewModel
        binding.userVm = userViewModel
        binding.songListVm = songListVm

        initUserContent()

        return view
    }

    override fun onPause() {
        super.onPause()
        binding.query.clearFocus()
    }

    override fun initSongList(binding: FragmentUserBinding): SongList {
        initFilterMenu()

        return SongList(
            requireActivity(),
            songListVm,
            binding.favoritesList.list,
            binding.favoritesList.refreshLayout,
            binding.query,
            LIST_ID,
            this::loadSongs
        )
    }

    private fun initFilterMenu() {
        binding.overflowBtn.setOnClickListener { view ->
            view.popupMenu(
                R.menu.menu_sort,
                {
                    songSortUtil.initSortMenu(LIST_ID, this)
                },
                songList::handleMenuItemClick
            )
        }
    }

    private fun loadSongs(adapter: SongsListAdapter) {
        launchIO {
            try {
                val favorites = radioClient.api.getUserFavorites()

                launchUI {
                    songList.showLoading(false)
                    adapter.songs = favorites
                    userViewModel.hasFavorites = favorites.isNotEmpty()
                }
            } catch (e: Exception) {
                launchUI { songList.showLoading(false) }
            }
        }
    }

    private fun initUserContent() {
        songList.showLoading(true)

        if (authUtil.isAuthenticated) {
            getUserInfo()
            songList.loadSongs()
        }
    }

    private fun getUserInfo() {
        launchIO {
            val user = radioClient.api.getUserInfo()

            userViewModel.user = user

            user.avatarImage?.let {
                userViewModel.avatarUrl = Library.CDN_AVATAR_URL + it
            }

            user.bannerImage?.let {
                userViewModel.bannerUrl = Library.CDN_BANNER_URL + it
            }
        }
    }
}

private const val LIST_ID = "FAVORITES_LIST"
