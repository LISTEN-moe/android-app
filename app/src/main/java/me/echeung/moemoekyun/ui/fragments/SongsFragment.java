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
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.adapters.SongsList;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;
import me.echeung.moemoekyun.viewmodels.SongsViewModel;

public class SongsFragment extends Fragment implements SongsList.SongListLoader {

    private static final String LIST_ID = "SEARCH_LIST";

    private FragmentSongsBinding binding;

    private SongsViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_songs, container, false);

        viewModel = App.getSongsViewModel();
        viewModel.reset();

        binding.setVm(viewModel);

        initSearchBar();

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    private void initSearchBar() {
        SongsList songsList = new SongsList(getActivity(), binding.songsList, LIST_ID, this);
        songsList.init();
    }

    @Override
    public void loadSongs(SongAdapter adapter) {
        App.getApiClient().search(null, new SearchCallback() {
            @Override
            public void onSuccess(final List<Song> results) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.setSongs(results);
                    });

                    viewModel.setHasResults(results != null && !results.isEmpty());
                }
            }

            @Override
            public void onFailure(final String message) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onFilter(String query, boolean hasResults) {
        viewModel.setQuery(query);
        viewModel.setHasResults(hasResults);
    }

}
