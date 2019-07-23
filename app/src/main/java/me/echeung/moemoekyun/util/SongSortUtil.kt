package me.echeung.moemoekyun.util

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.SongsAdapter
import me.echeung.moemoekyun.client.model.Song
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

    fun getComparator(context: Context, listId: String): Comparator<Song> {
        val sortType = getSortTypeByListId(context, listId)
        val sortDescending = getSortDescendingByListId(context, listId)

        return when (sortType) {
            SORT_ARTIST -> if (sortDescending)
                compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.artistsString ?: "" }
            else
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.artistsString ?: "" }

            // Default is SORT_TITLE
            else -> if (sortDescending)
                compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.titleString ?: "" }
            else
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.titleString ?: "" }
        }
    }

    fun initSortMenu(context: Context, listId: String, menu: Menu) {
        val sortType = getSortTypeByListId(context, listId)
        val sortTypeId = when (sortType) {
            SORT_ARTIST -> R.id.action_sort_type_artist
            // Default is SORT_TITLE
            else -> R.id.action_sort_type_title
        }
        menu.findItem(sortTypeId).isChecked = true

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
