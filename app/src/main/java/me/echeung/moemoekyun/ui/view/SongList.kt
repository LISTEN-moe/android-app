package me.echeung.moemoekyun.ui.view

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsAdapter
import me.echeung.moemoekyun.databinding.SongsListBinding
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.viewmodel.SongListViewModel
import java.lang.ref.WeakReference

class SongList(activity: Activity, binding: SongsListBinding, listId: String, private val loader: SongListLoader) {

    private val activity: WeakReference<Activity> = WeakReference(activity)
    private val adapter: SongsAdapter = SongsAdapter(activity, listId)
    private val swipeRefreshLayout: SwipeRefreshLayout?
    private val songListViewModel: SongListViewModel

    init {
        // List adapter
        val songsList = binding.list
        songsList.layoutManager = LinearLayoutManager(activity)
        songsList.adapter = adapter

        // Pull to refresh
        swipeRefreshLayout = binding.refreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener { this.loadSongs() })
        swipeRefreshLayout.isRefreshing = false

        // Only allow pull to refresh when user is at the top of the list
        songsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val topRowVerticalPosition = if (songsList.childCount != 0)
                    songsList.getChildAt(0).top
                else
                    0
                swipeRefreshLayout.isEnabled = topRowVerticalPosition >= 0
            }
        })

        this.songListViewModel = SongListViewModel()
        binding.vm = songListViewModel

        // Filter
        binding.query.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                val query = editable.toString().trim { it <= ' ' }.toLowerCase()
                adapter.filter(query)

                val hasResults = adapter.itemCount != 0
                songListViewModel.hasResults = hasResults
                if (hasResults) {
                    binding.list.scrollToPosition(0)
                }
            }
        })

        // Menu
        binding.overflowBtn.setOnClickListener(fun(_) {
            val activityRef = this.activity.get() ?: return

            val popupMenu = PopupMenu(activityRef, binding.overflowBtn)
            popupMenu.inflate(R.menu.menu_sort)

            SongSortUtil.initSortMenu(activityRef, listId, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { this.handleMenuItemClick(it) }
            popupMenu.show()
        })
    }

    fun loadSongs() {
        loader.loadSongs(adapter)
    }

    fun showLoading(loading: Boolean) {
        swipeRefreshLayout?.isRefreshing = loading
    }

    fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        val activityRef = activity.get() ?: return false

        if (SongSortUtil.handleSortMenuItem(item, adapter)) {
            return true
        }

        if (item.itemId == R.id.action_random_request) {
            val randomSong = adapter.randomRequestSong
            if (randomSong != null) {
                SongActionsUtil.request(activityRef, randomSong)
            }
            return true
        }

        return false
    }

    interface SongListLoader {
        fun loadSongs(adapter: SongsAdapter)
    }

}
