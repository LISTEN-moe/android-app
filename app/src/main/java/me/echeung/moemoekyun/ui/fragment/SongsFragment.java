package me.echeung.moemoekyun.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapter.songslist.SongAdapter;
import me.echeung.moemoekyun.adapter.songslist.SongList;
import me.echeung.moemoekyun.client.api.callback.SearchCallback;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.ui.base.BaseFragment;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class SongsFragment extends BaseFragment<FragmentSongsBinding> implements SongList.SongListLoader {

    private static final String LIST_ID = "SONGS_LIST";

    private SongList songList;

    @Override
    @LayoutRes
    public int getLayout() {
        return R.layout.fragment_songs;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        songList = new SongList(getActivity(), binding.songsList, LIST_ID, this);
        songList.loadSongs();

        return view;
    }

    @Override
    public BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case MainActivity.AUTH_EVENT:
                            songList.loadSongs();
                            break;

                        case SongActionsUtil.FAVORITE_EVENT:
                            songList.notifyDataSetChanged();
                            break;
                    }
                }
            }
        };
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(SongActionsUtil.FAVORITE_EVENT);

        return intentFilter;
    }

    @Override
    public void loadSongs(SongAdapter adapter) {
        songList.showLoading(true);

        App.getApiClient().search(null, new SearchCallback() {
            @Override
            public void onSuccess(List<Song> results) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                        adapter.setSongs(results);
                    });
                }
            }

            @Override
            public void onFailure(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

}
