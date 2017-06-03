package jcotter.listenmoe.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import jcotter.listenmoe.R;
import jcotter.listenmoe.adapters.SongAdapter;
import jcotter.listenmoe.interfaces.SearchCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.SongActionsUtil;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnSongItemClickListener {

    private RecyclerView mResultsList;
    private LinearLayout mNoResults;
    private TextView mNoResultsMsg;

    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set up app bar
        setSupportActionBar((Toolbar) findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // UI view references
        mResultsList = (RecyclerView) findViewById(R.id.search_results);
        mNoResults = (LinearLayout) findViewById(R.id.no_results);
        mNoResultsMsg = (TextView) findViewById(R.id.no_results_msg);

        initSearch();
    }

    private void initSearch() {
        adapter = new SongAdapter(this);

        mResultsList.setLayoutManager(new LinearLayoutManager(this));
        mResultsList.setAdapter(adapter);

        final EditText mSearchQuery = (EditText) findViewById(R.id.search_query);
        mSearchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String query = v.getText().toString().trim();
                APIUtil.search(getBaseContext(), query, new SearchCallback() {
                    @Override
                    public void onFailure(final String result) {
                    }

                    @Override
                    public void onSuccess(final List<Song> results) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setSongs(results);
                                toggleEmptyView(results.size() == 0, query);
                            }
                        });
                    }
                });
                return true;
            }
        });
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
                .setNegativeButton(favoriteAction, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int in) {
                        SongActionsUtil.favorite(SearchActivity.this, adapter, song);
                    }
                });

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                    SongActionsUtil.request(SearchActivity.this, adapter, song);
                }
            });
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
