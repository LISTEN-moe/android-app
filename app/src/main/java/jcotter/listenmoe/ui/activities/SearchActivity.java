package jcotter.listenmoe.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jcotter.listenmoe.R;
import jcotter.listenmoe.adapters.SongAdapter;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.interfaces.OnSongItemClickListener;
import jcotter.listenmoe.interfaces.RequestSongCallback;
import jcotter.listenmoe.interfaces.SearchCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.util.APIUtil;

public class SearchActivity extends AppCompatActivity implements OnSongItemClickListener {

    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Handle up action
        findViewById(R.id.search_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initSearch();
    }

    private void initSearch() {
        adapter = new SongAdapter(this);

        final RecyclerView mResults = (RecyclerView) findViewById(R.id.search_results);
        mResults.setLayoutManager(new LinearLayoutManager(this));
//        mList.addItemDecoration(new DividerItemDecoration(getContext()));
        mResults.setAdapter(adapter);

        final EditText mSearchQuery = (EditText) findViewById(R.id.search_query);
        mSearchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                APIUtil.search(getBaseContext(), v.getText().toString().trim(), new SearchCallback() {
                    @Override
                    public void onFailure(final String result) {
                    }

                    @Override
                    public void onSuccess(final List<Song> results) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setSongs(results);
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
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(favoriteAction, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int in) {
                                favorite(song);
                    }
                });

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                            request(song);
                }
            });
        }

        builder.create().show();
    }


    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    private void favorite(final Song song) {
        APIUtil.favoriteSong(this, song.getId(), new FavoriteSongCallback() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), R.string.req_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(final boolean favorited) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        song.setFavorite(favorited);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    private void request(final Song song) {
        APIUtil.requestSong(this, song.getId(), new RequestSongCallback() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.equals(ResponseMessages.USER_NOT_SUPPORTER)) {
                            Toast.makeText(getBaseContext(), R.string.supporter, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), R.string.req_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), R.string.success, Toast.LENGTH_LONG).show();

                        song.setEnabled(false);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
