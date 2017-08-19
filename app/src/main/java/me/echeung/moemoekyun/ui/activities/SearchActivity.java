package me.echeung.moemoekyun.ui.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.databinding.ActivitySearchBinding;
import me.echeung.moemoekyun.interfaces.SearchListener;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.state.SearchState;
import me.echeung.moemoekyun.util.APIUtil;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnSongItemClickListener {

    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivitySearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        final SearchState state = SearchState.getInstance();
        state.reset();

        binding.setHasResults(state.hasResults);
        binding.setQuery(state.query);

        // Set up app bar
        setSupportActionBar(binding.appbar);
        ActionBar appBar = getSupportActionBar();
        appBar.setDisplayHomeAsUpEnabled(true);
        appBar.setDisplayShowTitleEnabled(false);

        // Set up search
        binding.searchQuery.setOnEditorActionListener(this::onEditorAction);
        binding.btnClearSearch.setOnClickListener(v -> binding.searchQuery.setText(null));

        // Results list adapter
        adapter = new SongAdapter(this);
        binding.resultsList.setLayoutManager(new LinearLayoutManager(this));
        binding.resultsList.setAdapter(adapter);

        // TODO: the GIF looks pretty bad
        // https://github.com/bumptech/glide/issues/2271
        // No results image
        Glide.with(this)
                .load(R.drawable.kanna_dancing)
                .into(binding.searchPlaceholder);
    }

    private boolean onEditorAction(TextView textView, int i, KeyEvent event) {
        final String query = textView.getText().toString().trim();

        if (!query.isEmpty()) {
            APIUtil.search(getBaseContext(), query, new SearchListener() {
                @Override
                public void onFailure(final String result) {
                    updateResults(query, null);
                }

                @Override
                public void onSuccess(final List<Song> results) {
                    runOnUiThread(() -> {
                        updateResults(query, results);
                    });
                }
            });
        }

        return true;
    }

    private void updateResults(final String query, final List<Song> results) {
        adapter.setSongs(results);
        SearchState.getInstance().query.set(query);
        SearchState.getInstance().hasResults.set(results != null && results.size() != 0);
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
}
