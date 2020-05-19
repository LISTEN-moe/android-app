package me.echeung.moemoekyun.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsListAdapter
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.databinding.FragmentUserBinding
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil
import me.echeung.moemoekyun.ui.base.SongsListBaseFragment
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.system.launchIO
import me.echeung.moemoekyun.util.system.launchUI
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.android.inject

class UserFragment : SongsListBaseFragment<FragmentUserBinding>(), SongList.SongListLoader, SharedPreferences.OnSharedPreferenceChangeListener {

    private val radioClient: RadioClient by inject()
    private val authUtil: AuthUtil by inject()
    private val songSortUtil: SongSortUtil by inject()

    private val radioViewModel: RadioViewModel by inject()
    private val userViewModel: UserViewModel by inject()

    init {
        layout = R.layout.fragment_user

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action != null) {
                    when (action) {
                        AuthActivityUtil.AUTH_EVENT, SongActionsUtil.FAVORITE_EVENT -> initUserContent()
                    }
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

    override fun initSongList(binding: FragmentUserBinding): SongList {
        initFilterMenu()

        return SongList(
                requireActivity(),
                songListVm,
                binding.favorites.favoritesList.list,
                binding.favorites.favoritesList.refreshLayout,
                binding.favorites.filter.query,
                LIST_ID,
                this)
    }

    private fun initFilterMenu() {
        val overflowBtn = binding.favorites.filter.overflowBtn

        overflowBtn.setOnClickListener(fun(_) {
            val popupMenu = PopupMenu(requireContext(), overflowBtn)
            popupMenu.inflate(R.menu.menu_sort)

            songSortUtil.initSortMenu(LIST_ID, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { songList.handleMenuItemClick(it) }
            popupMenu.show()
        })
    }

    override fun loadSongs(adapter: SongsListAdapter) {
        launchIO {
            radioClient.api.getUserFavorites(object : UserFavoritesCallback {
                override fun onSuccess(favorites: List<Song>) {
                    launchUI {
                        songList.showLoading(false)
                        adapter.songs = favorites
                        userViewModel.hasFavorites = favorites.isNotEmpty()
                    }
                }

                override fun onFailure(message: String?) {
                    launchUI { songList.showLoading(false) }
                }
            })
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
            radioClient.api.getUserInfo(object : UserInfoCallback {
                override fun onSuccess(user: User) {
                    userViewModel.user = user

                    if (user.avatarImage != null) {
                        userViewModel.avatarUrl = Library.CDN_AVATAR_URL + user.avatarImage
                    }

                    if (user.bannerImage != null) {
                        userViewModel.bannerUrl = Library.CDN_BANNER_URL + user.bannerImage
                    }
                }

                override fun onFailure(message: String?) {}
            })
        }
    }

    companion object {
        private const val LIST_ID = "FAVORITES_LIST"
    }
}
