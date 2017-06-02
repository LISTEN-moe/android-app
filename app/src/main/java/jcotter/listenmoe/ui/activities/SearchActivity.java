package jcotter.listenmoe.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import jcotter.listenmoe.R;
import jcotter.listenmoe.adapters.SongAdapter;
import jcotter.listenmoe.interfaces.OnSongItemClickListener;
import jcotter.listenmoe.interfaces.SearchCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.SongActionsUtil;

public class SearchActivity extends AppCompatActivity implements OnSongItemClickListener {

    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set up app bar
        Toolbar mAppbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(mAppbar);

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initSearch();
    }

    private void initSearch() {
        adapter = new SongAdapter(this);

        final RecyclerView mResults = (RecyclerView) findViewById(R.id.search_results);
        mResults.setLayoutManager(new LinearLayoutManager(this));
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
                        SongActionsUtil.favorite(getParent(), adapter, song);
                    }
                });

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                    SongActionsUtil.request(getParent(), adapter, song);
                }
            });
        }

        builder.create().show();
    }
}
