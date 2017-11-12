package me.echeung.moemoekyun.ui.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import me.echeung.listenmoeapi.callbacks.SearchCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.databinding.ActivitySearchBinding;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.utils.SongSortUtil;
import me.echeung.moemoekyun.viewmodels.SearchViewModel;

public class SearchActivity extends BaseActivity implements SongAdapter.OnSongItemClickListener {

    private static final String LIST_ID = "SEARCH_LIST";

    private ActivitySearchBinding binding;

    private SearchViewModel viewModel;

    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        viewModel = App.getSearchViewModel();
        viewModel.reset();

        binding.setVm(viewModel);

        // Set up app bar
        setSupportActionBar(binding.appbar);
        final ActionBar appBar = getSupportActionBar();
        appBar.setDisplayHomeAsUpEnabled(true);
        appBar.setDisplayShowTitleEnabled(false);

        // Set up search
        binding.btnClearSearch.setOnClickListener(v -> binding.searchQuery.setText(""));
        binding.searchQuery.setOnEditorActionListener(this::onEditorAction);
        binding.searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setShowClearButton(editable.length() != 0);
            }
        });

        // Results list adapter
        adapter = new SongAdapter(this, LIST_ID, this);
        binding.resultsList.setLayoutManager(new LinearLayoutManager(this));
        binding.resultsList.setAdapter(adapter);

        // No results image
        Glide.with(this)
                .load(R.drawable.kanna_dancing)
                .into(binding.searchPlaceholder);
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SongSortUtil.initSortMenu(this, LIST_ID, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (SongSortUtil.handleSortMenuItem(item, adapter)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean onEditorAction(TextView textView, int i, KeyEvent event) {
        final String query = textView.getText().toString().trim();

        if (!TextUtils.isEmpty(query)) {
            App.getApiClient().search(query, new SearchCallback() {
                @Override
                public void onSuccess(final List<Song> results) {
                    runOnUiThread(() -> {
                        updateResults(query, results);
                    });
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
        viewModel.setQuery(query);
        viewModel.setHasResults(results != null && !results.isEmpty());
    }

    @Override
    public void onSongItemClick(final Song song) {
        final String favoriteAction = song.isFavorite() ?
                getString(R.string.action_unfavorite) :
                getString(R.string.action_favorite);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme)
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
