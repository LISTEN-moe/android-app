package me.echeung.moemoekyun.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.LayoutRes;
import android.widget.Toast;

import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapter.SongAdapter;
import me.echeung.moemoekyun.client.api.callback.SearchCallback;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.ui.base.SongsListBaseFragment;
import me.echeung.moemoekyun.ui.view.SongList;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class SongsFragment extends SongsListBaseFragment<FragmentSongsBinding> implements SongList.SongListLoader, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    @LayoutRes
    public int getLayout() {
        return R.layout.fragment_songs;
    }

    @Override
    public SongList initSongList(FragmentSongsBinding binding) {
        return new SongList(getActivity(), binding.songsList,  "SONGS_LIST", this);
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
