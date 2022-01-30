package me.echeung.moemoekyun.ui.view

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsListAdapter
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.viewmodel.SongListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

class SongList(
    activity: Activity,
    private val songListViewModel: SongListViewModel,
    private val songsList: RecyclerView,
    private val swipeRefreshLayout: SwipeRefreshLayout?,
    filterView: View,
    listId: String,
    private val loadSongs: (adapter: SongsListAdapter) -> Unit
) : KoinComponent {

    private val songActionsUtil: SongActionsUtil by inject()
    private val songSortUtil: SongSortUtil by inject()

    private val activity: WeakReference<Activity> = WeakReference(activity)
    private val adapter: SongsListAdapter = SongsListAdapter(activity, listId)

    init {
        // List adapter
        songsList.layoutManager = LinearLayoutManager(activity)
        songsList.adapter = adapter

        // Pull to refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
            swipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener { this.loadSongs() })
            swipeRefreshLayout.isRefreshing = false

            // Only allow pull to refresh when user is at the top of the list
            songsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val topRowVerticalPosition = if (songsList.childCount != 0) {
                        songsList.getChildAt(0).top
                    } else {
                        0
                    }
                    swipeRefreshLayout.isEnabled = topRowVerticalPosition >= 0
                }
            })
        }

        // Filter
        if (filterView is EditText) {
            filterView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun afterTextChanged(editable: Editable) {
                    handleQuery(editable.toString())
                }
            })
        }

        if (filterView is SearchView) {
            filterView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    handleQuery(query!!)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    handleQuery(newText!!)
                    return true
                }
            })

            filterView.setOnCloseListener {
                handleQuery("")
                true
            }
        }
    }

    private fun handleQuery(query: String) {
        val trimmedQuery = query.trim { it <= ' ' }.lowercase()

        adapter.filter(trimmedQuery)

        val hasResults = adapter.itemCount != 0
        songListViewModel.hasResults = hasResults
        if (hasResults) {
            songsList.scrollToPosition(0)
        }
    }

    fun loadSongs() {
        loadSongs(adapter)
    }

    fun showLoading(loading: Boolean) {
        swipeRefreshLayout?.isRefreshing = loading
    }

    fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    fun handleMenuItemClick(item: MenuItem): Boolean {
        val activityRef = activity.get() ?: return false

        if (songSortUtil.handleSortMenuItem(item, adapter)) {
            return true
        }

        if (item.itemId == R.id.action_random_request) {
            adapter.randomRequestSong?.let {
                songActionsUtil.request(activityRef, it)
            }
            return true
        }

        return false
    }
}
