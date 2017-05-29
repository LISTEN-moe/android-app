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
import jcotter.listenmoe.adapters.SearchAdapter;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.interfaces.RequestSongCallback;
import jcotter.listenmoe.interfaces.SearchCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.util.APIUtil;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnItemClickListener {

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
        final SearchAdapter searchAdapter = new SearchAdapter(this);

        final RecyclerView mResults = (RecyclerView) findViewById(R.id.search_results);
        mResults.setLayoutManager(new LinearLayoutManager(this));
//        mList.addItemDecoration(new DividerItemDecoration(getContext()));
        mResults.setAdapter(searchAdapter);

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
                                searchAdapter.setResults(results);
                            }
                        });
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onItemClick(final Song song) {
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
                                favorite(song.getId());
                    }
                });

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                            request(song.getId());
                }
            });
        }

        builder.create().show();
    }


    /**
     * Updates the favorite status of a song.
     *
     * @param songID ID of the song to update the favorite status of.
     */
    private void favorite(final int songID) {
        APIUtil.favoriteSong(this, songID, new FavoriteSongCallback() {
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
//                        if (favorited) {
//                            favorite.set(songIndex, 1);
//                        } else {
//                            favorite.set(songIndex, 0);
//
//                            // Remove song from favorites list
//                            if (tabHost.getCurrentTab() == 1) {
//                                songIds.remove(songIndex);
//                                favorite.remove(songIndex);
//                                enabled.remove(songIndex);
//                                adapter.remove(adapter.getItem(songIndex));
//                            }
//                        }
//                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * Requests a song.
     *
     * @param songID ID of the song to request.
     */
    private void request(final int songID) {
        APIUtil.requestSong(this, songID, new RequestSongCallback() {
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

//                        enabled.set(songIndex, false);
//                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
