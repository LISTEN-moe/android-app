package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.songslist.SongAdapter;

public final class SongSortUtil {

    private static final String PREF_LIST_PREFIX_TYPE = "song_sort_list_type_";
    private static final String PREF_LIST_PREFIX_DESC = "song_sort_list_desc_";

    private static final String SORT_TITLE = "song_sort_title";
    private static final String SORT_ARTIST = "song_sort_artist";

    public static void setListSortType(final Context context, final String listId, final String sortType) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit()
                .putString(PREF_LIST_PREFIX_TYPE + listId, sortType)
                .apply();
    }

    public static void setListSortDescending(final Context context, final String listId, final boolean descending) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit()
                .putBoolean(PREF_LIST_PREFIX_DESC + listId, descending)
                .apply();
    }

    public static void sort(final Context context, final String listId, final List<Song> songs) {
        final String sortType = getSortTypeByListId(context, listId);
        final boolean sortDescending = getSortDescendingByListId(context, listId);

        Comparator<Song> sorter;
        switch (sortType) {
            case SORT_ARTIST:
                sorter = sortDescending
                        ? (song, t1) -> t1.getArtistsString().compareToIgnoreCase(song.getArtistsString())
                        : (song, t1) -> song.getArtistsString().compareToIgnoreCase(t1.getArtistsString());
                break;

            case SORT_TITLE:
            default:
                sorter = sortDescending
                        ? (song, t1) -> t1.getTitle().compareToIgnoreCase(song.getTitle())
                        : (song, t1) -> song.getTitle().compareToIgnoreCase(t1.getTitle());
                break;
        }

        Collections.sort(songs, sorter);
    }

    public static void initSortMenu(Context context, String listId, Menu menu) {
        final String sortType = getSortTypeByListId(context, listId);
        switch (sortType) {
            case SORT_ARTIST:
                menu.findItem(R.id.action_sort_type_artist).setChecked(true);
                break;

            case SORT_TITLE:
            default:
                menu.findItem(R.id.action_sort_type_title).setChecked(true);
                break;
        }

        final boolean sortDescending = getSortDescendingByListId(context, listId);
        menu.findItem(R.id.action_sort_desc).setChecked(sortDescending);
    }

    public static boolean handleSortMenuItem(MenuItem item, SongAdapter adapter) {
        switch (item.getItemId()) {
            case R.id.action_sort_desc:
                item.setChecked(!item.isChecked());
                adapter.sortDescending(item.isChecked());
                return true;

            case R.id.action_sort_type_title:
                item.setChecked(true);
                adapter.sortType(SongSortUtil.SORT_TITLE);
                return true;

            case R.id.action_sort_type_artist:
                item.setChecked(true);
                adapter.sortType(SongSortUtil.SORT_ARTIST);
                return true;
        }

        return false;
    }

    private static String getSortTypeByListId(final Context context, final String listKey) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_LIST_PREFIX_TYPE + listKey, SORT_TITLE);
    }

    private static boolean getSortDescendingByListId(final Context context, final String listKey) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(PREF_LIST_PREFIX_DESC + listKey, false);
    }

}
