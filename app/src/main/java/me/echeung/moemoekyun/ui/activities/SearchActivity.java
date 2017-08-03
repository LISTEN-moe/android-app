package me.echeung.moemoekyun.ui.activities;

import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.List;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.databinding.SearchActivityBinding;
import me.echeung.moemoekyun.interfaces.SearchListener;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.util.APIUtil;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnSongItemClickListener {

    private SongAdapter adapter;
    private SearchState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.search_activity);

        // Data binding state
        state = new SearchState();
        binding.setHasResults(state.hasResults);
        binding.setQuery(state.query);

        // Set up app bar
        setSupportActionBar(binding.appbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.searchQuery.setOnEditorActionListener(this::onEditorAction);

        // Results list adapter
        adapter = new SongAdapter(this);
        binding.resultsList.setLayoutManager(new LinearLayoutManager(this));
        binding.resultsList.setAdapter(adapter);
    }

    private boolean onEditorAction(TextView textView, int i, KeyEvent event) {
        final String query = textView.getText().toString().trim();

        APIUtil.search(getBaseContext(), query, new SearchListener() {
            @Override
            public void onFailure(final String result) {
                state.query.set(query);
            }

            @Override
            public void onSuccess(final List<Song> results) {
                runOnUiThread(() -> {
                    adapter.setSongs(results);
                    state.query.set(query);
                    state.hasResults.set(results.size() != 0);
                });
            }
        });

        return true;
    }

    @Override
    public void onSongItemClick(final Song song) {
        // Create button "Favorite"/"Unfavorite"
        final String favoriteAction = song.isFavorite() ?
                getString(R.string.action_unfavorite) :
                getString(R.string.action_favorite);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(song.getTitle())
                .setMessage(song.getArtistAndAnime())
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(favoriteAction,
                        (dialogInterface, in) -> SongActionsUtil.favorite(SearchActivity.this, adapter, song));

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request),
                    (dialogInterface, im) -> SongActionsUtil.request(SearchActivity.this, adapter, song));
        }

        builder.create().show();
    }

    private class SearchState extends BaseObservable {
        public final ObservableBoolean hasResults = new ObservableBoolean(true);
        public final ObservableField<String> query = new ObservableField<>();
    }
}
