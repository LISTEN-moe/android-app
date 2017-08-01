package me.echeung.listenmoe.ui.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import me.echeung.listenmoe.R;
import me.echeung.listenmoe.adapters.SongAdapter;
import me.echeung.listenmoe.databinding.MainActivityBinding;
import me.echeung.listenmoe.databinding.SearchActivityBinding;
import me.echeung.listenmoe.interfaces.SearchListener;
import me.echeung.listenmoe.model.Song;
import me.echeung.listenmoe.util.APIUtil;
import me.echeung.listenmoe.util.SongActionsUtil;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnSongItemClickListener {

    @BindView(R.id.search_results)
    RecyclerView mResultsList;
    @BindView(R.id.no_results)
    LinearLayout mNoResults;
    @BindView(R.id.no_results_msg)
    TextView mNoResultsMsg;

    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.search_activity);
        ButterKnife.bind(this);

        // Set up app bar
        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Results list adapter
        adapter = new SongAdapter(this);
        mResultsList.setLayoutManager(new LinearLayoutManager(this));
        mResultsList.setAdapter(adapter);
    }

    @OnEditorAction(R.id.search_query)
    public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
        final String query = textView.getText().toString().trim();
        APIUtil.search(getBaseContext(), query, new SearchListener() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final List<Song> results) {
                runOnUiThread(() -> {
                    adapter.setSongs(results);
                    toggleEmptyView(results.size() == 0, query);
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
                .setMessage(R.string.req_dialog_message)
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(favoriteAction, (dialogInterface, in) -> SongActionsUtil.favorite(SearchActivity.this, adapter, song));

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), (dialogInterface, im) -> SongActionsUtil.request(SearchActivity.this, adapter, song));
        }

        builder.create().show();
    }

    private void toggleEmptyView(boolean isEmpty, String query) {
        mResultsList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mNoResults.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (isEmpty) {
            mNoResultsMsg.setText(String.format(getString(R.string.no_results), query));
        }
    }
}
