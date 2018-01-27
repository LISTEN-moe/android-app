package me.echeung.moemoekyun.ui.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.echeung.listenmoeapi.callbacks.SearchCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.databinding.FragmentSongsBinding;
import me.echeung.moemoekyun.utils.SearchBarUtil;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.viewmodels.SongsViewModel;

public class SongsFragment extends Fragment implements SongAdapter.OnSongItemClickListener {

    private static final String LIST_ID = "SEARCH_LIST";

    private FragmentSongsBinding binding;

    private SongsViewModel viewModel;

    private SongAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_songs, container, false);

        viewModel = App.getSongsViewModel();
        viewModel.reset();

        binding.setVm(viewModel);

        // Results list adapter
        adapter = new SongAdapter(getContext(), LIST_ID, this);
        binding.resultsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.resultsList.setAdapter(adapter);

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
        SearchBarUtil searchBarUtil = new SearchBarUtil(getActivity(), binding.songsSearchBar, adapter, LIST_ID)
                .withOnEditorActionListener(this::onEditorAction);

        searchBarUtil.init();
    }

    private boolean onEditorAction(TextView textView, int i, KeyEvent event) {
        final String query = textView.getText().toString().trim();

        if (!TextUtils.isEmpty(query)) {
            App.getApiClient().search(query, new SearchCallback() {
                @Override
                public void onSuccess(final List<Song> results) {
                    getActivity().runOnUiThread(() -> updateResults(query, results));
                }

                @Override
                public void onFailure(final String message) {
                    updateResults(query, null);
                }
            });
        }

        return true;
    }

    private void updateResults(final String query, final List<Song> results) {
        adapter.setSongs(results);
//        viewModel.setQuery(query);
        viewModel.setHasResults(results != null && !results.isEmpty());
    }

    @Override
    public void onSongItemClick(final Song song) {
        SongActionsUtil.showSongActionsDialog(getActivity(), adapter, song);
    }

}
