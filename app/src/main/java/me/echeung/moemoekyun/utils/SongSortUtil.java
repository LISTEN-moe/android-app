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
import me.echeung.moemoekyun.adapters.SongAdapter;

public class SongSortUtil {

    private static final String PREF_LIST_PREFIX = "song_sort_list_";
    private static final String SORT_TITLE = "song_sort_title";
    private static final String SORT_TITLE_DESC = SORT_TITLE + ".desc";
    private static final String SORT_ARTIST = "song_sort_artist";
    private static final String SORT_ARTIST_DESC = SORT_ARTIST + ".desc";

    public static void setListSort(final Context context, final String listId, final String sortType) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit()
                .putString(PREF_LIST_PREFIX + listId, sortType)
                .apply();
    }

    public static void sort(final Context context, final String listId, final List<Song> songs) {
        // Default is SORT_TITLE
        Comparator<Song> sorter = (song, t1) -> song.getTitle().compareToIgnoreCase(t1.getTitle());

        final String sortType = getSortByListId(context, listId);
        switch (sortType) {
            case SORT_ARTIST:
                sorter = (song, t1) -> song.getArtist().compareToIgnoreCase(t1.getArtist());
                break;

            case SORT_ARTIST_DESC:
                sorter = (song, t1) -> t1.getArtist().compareToIgnoreCase(song.getArtist());
                break;

            case SORT_TITLE_DESC:
                sorter = (song, t1) -> t1.getTitle().compareToIgnoreCase(song.getTitle());
                break;
        }

        Collections.sort(songs, sorter);
    }

    public static void initSortMenu(Context context, String listId, Menu menu) {
        final String sortType = getSortByListId(context, listId);
        switch (sortType) {
            case SORT_ARTIST:
                menu.findItem(R.id.action_sort_type_artist).setChecked(true);
                break;

            case SORT_ARTIST_DESC:
                menu.findItem(R.id.action_sort_type_artist_desc).setChecked(true);
                break;

            case SORT_TITLE_DESC:
                menu.findItem(R.id.action_sort_type_title_desc).setChecked(true);
                break;

            case SORT_TITLE:
            default:
                menu.findItem(R.id.action_sort_type_title).setChecked(true);
                break;
        }
    }

    public static boolean handleSortMenuItem(MenuItem item, SongAdapter adapter) {
        switch (item.getItemId()) {
            case R.id.action_sort_type_title:
                adapter.sort(SongSortUtil.SORT_TITLE);
                item.setChecked(true);
                return true;

            case R.id.action_sort_type_title_desc:
                adapter.sort(SongSortUtil.SORT_TITLE_DESC);
                item.setChecked(true);
                return true;

            case R.id.action_sort_type_artist:
                adapter.sort(SongSortUtil.SORT_ARTIST);
                item.setChecked(true);
                return true;

            case R.id.action_sort_type_artist_desc:
                adapter.sort(SongSortUtil.SORT_ARTIST_DESC);
                item.setChecked(true);
                return true;
        }

        return false;
    }

    private static String getSortByListId(final Context context, final String listKey) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_LIST_PREFIX + listKey, SORT_TITLE);
    }
}
