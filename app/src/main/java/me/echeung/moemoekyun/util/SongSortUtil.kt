package me.echeung.moemoekyun.util

import android.content.Context
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsAdapter
import me.echeung.moemoekyun.client.model.Song
import java.util.Collections
import java.util.Comparator

object SongSortUtil {

    private const val PREF_LIST_PREFIX_TYPE = "song_sort_list_type_"
    private const val PREF_LIST_PREFIX_DESC = "song_sort_list_desc_"

    private const val SORT_TITLE = "song_sort_title"
    private const val SORT_ARTIST = "song_sort_artist"

    fun setListSortType(context: Context, listId: String, sortType: String) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPrefs.edit()
                .putString(PREF_LIST_PREFIX_TYPE + listId, sortType)
                .apply()
    }

    fun setListSortDescending(context: Context, listId: String, descending: Boolean) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPrefs.edit()
                .putBoolean(PREF_LIST_PREFIX_DESC + listId, descending)
                .apply()
    }

    fun sort(context: Context, listId: String, songs: List<Song>) {
        val sortType = getSortTypeByListId(context, listId)
        val sortDescending = getSortDescendingByListId(context, listId)

        val sorter: Comparator<Song> = when (sortType) {
            SORT_ARTIST -> if (sortDescending)
                Comparator { song, t1 -> t1.artistsString.compareTo(song.artistsString, ignoreCase = true) }
            else
                Comparator { song, t1 -> song.artistsString.compareTo(t1.artistsString, ignoreCase = true) }

            SORT_TITLE -> if (sortDescending)
                Comparator { song, t1 -> t1.titleString!!.compareTo(song.titleString!!, ignoreCase = true) }
            else
                Comparator { song, t1 -> song.titleString!!.compareTo(t1.titleString!!, ignoreCase = true) }
            else -> if (sortDescending)
                Comparator { song, t1 -> t1.titleString!!.compareTo(song.titleString!!, ignoreCase = true) }
            else
                Comparator { song, t1 -> song.titleString!!.compareTo(t1.titleString!!, ignoreCase = true) }
        }

        Collections.sort(songs, sorter)
    }

    fun initSortMenu(context: Context, listId: String, menu: Menu) {
        val sortType = getSortTypeByListId(context, listId)
        when (sortType) {
            SORT_ARTIST -> menu.findItem(R.id.action_sort_type_artist).isChecked = true

            SORT_TITLE -> menu.findItem(R.id.action_sort_type_title).isChecked = true
            else -> menu.findItem(R.id.action_sort_type_title).isChecked = true
        }

        val sortDescending = getSortDescendingByListId(context, listId)
        menu.findItem(R.id.action_sort_desc).isChecked = sortDescending
    }

    fun handleSortMenuItem(item: MenuItem, adapter: SongsAdapter): Boolean {
        when (item.itemId) {
            R.id.action_sort_desc -> {
                item.isChecked = !item.isChecked
                adapter.sortDescending(item.isChecked)
                return true
            }

            R.id.action_sort_type_title -> {
                item.isChecked = true
                adapter.sortType(SongSortUtil.SORT_TITLE)
                return true
            }

            R.id.action_sort_type_artist -> {
                item.isChecked = true
                adapter.sortType(SongSortUtil.SORT_ARTIST)
                return true
            }
        }

        return false
    }

    private fun getSortTypeByListId(context: Context, listKey: String): String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getString(PREF_LIST_PREFIX_TYPE + listKey, SORT_TITLE)!!
    }

    private fun getSortDescendingByListId(context: Context, listKey: String): Boolean {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getBoolean(PREF_LIST_PREFIX_DESC + listKey, false)
    }
}
