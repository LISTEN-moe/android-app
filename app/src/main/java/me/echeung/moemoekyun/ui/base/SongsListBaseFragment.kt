package me.echeung.moemoekyun.ui.base

import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.ui.view.SongList
import me.echeung.moemoekyun.util.AuthUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.viewmodel.SongListViewModel

abstract class SongsListBaseFragment<T : ViewDataBinding> : BaseFragment<T>(), SongList.SongListLoader, SharedPreferences.OnSharedPreferenceChangeListener {

    protected lateinit var songList: SongList
    protected val songListVm = SongListViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        songList = initSongList(binding)
        songList.loadSongs()

        App.preferenceUtil!!.registerListener(this)

        return view
    }

    abstract fun initSongList(binding: T): SongList

    override fun onDestroy() {
        App.preferenceUtil!!.unregisterListener(this)

        super.onDestroy()
    }

    public override fun getIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(AuthUtil.AUTH_EVENT)
        intentFilter.addAction(SongActionsUtil.FAVORITE_EVENT)

        return intentFilter
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.PREF_GENERAL_ROMAJI -> songList.notifyDataSetChanged()
        }
    }
}
