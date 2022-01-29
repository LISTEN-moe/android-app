package me.echeung.moemoekyun.ui.base

import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.viewmodel.SongListViewModel
import org.koin.android.ext.android.inject

abstract class SongsListBaseFragment<T : ViewDataBinding> : BaseFragment<T>() {

    private val scope = MainScope()

    private val preferenceUtil: PreferenceUtil by inject()

    protected lateinit var songList: SongList
    protected val songListVm = SongListViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        songList = initSongList(binding)
        songList.loadSongs()

        preferenceUtil.shouldPreferRomaji().asFlow()
            .onEach { songList.notifyDataSetChanged() }
            .launchIn(scope)

        return view
    }

    abstract fun initSongList(binding: T): SongList

    public override fun getIntentFilter() = IntentFilter().apply {
        addAction(AuthActivityUtil.AUTH_EVENT)
        addAction(SongActionsUtil.FAVORITE_EVENT)
    }
}
