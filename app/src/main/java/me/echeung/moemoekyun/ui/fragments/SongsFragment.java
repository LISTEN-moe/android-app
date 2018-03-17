package me.echeung.moemoekyun.ui.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import me.echeung.listenmoeapi.callbacks.SearchCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.songslist.SongAdapter;
import me.echeung.moemoekyun.adapters.songslist.SongList;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;

public class SongsFragment extends Fragment implements SongList.SongListLoader {

    private static final String LIST_ID = "SONGS_LIST";

    private FragmentSongsBinding binding;

    private SongList songList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_songs, container, false);

        songList = new SongList(getActivity(), binding.songsList, LIST_ID, this);
        songList.loadSongs();

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
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
