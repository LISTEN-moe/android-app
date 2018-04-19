package me.echeung.moemoekyun.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.songslist.SongAdapter;
import me.echeung.moemoekyun.adapters.songslist.SongList;
import me.echeung.moemoekyun.api.callbacks.SearchCallback;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;
import me.echeung.moemoekyun.models.Song;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.utils.SongActionsUtil;

public class SongsFragment extends Fragment implements SongList.SongListLoader {

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
                final String action = intent.getAction();
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
            public void onSuccess(final List<Song> results) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                        adapter.setSongs(results);
                    });
                }
            }

            @Override
            public void onFailure(final String message) {
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
