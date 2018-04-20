package me.echeung.moemoekyun.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapter.songslist.SongAdapter;
import me.echeung.moemoekyun.adapter.songslist.SongList;
import me.echeung.moemoekyun.api.callback.SearchCallback;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.ui.base.BaseFragment;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class SongsFragment extends BaseFragment implements SongList.SongListLoader {

    private static final String LIST_ID = "SONGS_LIST";

    private FragmentSongsBinding binding;

    private SongList songList;

    // Receiver
    private IntentFilter intentFilter;
    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_songs, container, false);

        songList = new SongList(getActivity(), binding.songsList, LIST_ID, this);
        songList.loadSongs();

        initBroadcastReceiver();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!receiverRegistered) {
            getActivity().registerReceiver(intentReceiver, intentFilter);
            receiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    private void initBroadcastReceiver() {
        intentReceiver = new BroadcastReceiver() {
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

        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(SongActionsUtil.FAVORITE_EVENT);

        getActivity().registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
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
