package me.echeung.moemoekyun.ui.base;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.ui.view.SongList;
import me.echeung.moemoekyun.util.PreferenceUtil;
import me.echeung.moemoekyun.util.SongActionsUtil;

public abstract class SongsListBaseFragment<T extends ViewDataBinding> extends BaseFragment<T> implements SongList.SongListLoader, SharedPreferences.OnSharedPreferenceChangeListener {

    protected SongList songList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        songList = initSongList(binding);
        songList.loadSongs();

        App.getPreferenceUtil().registerListener(this);

        return view;
    }

    public abstract SongList initSongList(T binding);

    @Override
    public void onDestroy() {
        App.getPreferenceUtil().unregisterListener(this);

        super.onDestroy();
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(SongActionsUtil.FAVORITE_EVENT);

        return intentFilter;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.PREF_GENERAL_ROMAJI:
                songList.notifyDataSetChanged();
                break;
        }
    }

}
