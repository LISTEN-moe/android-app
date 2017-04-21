package jcotter.listenmoe.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.AuthCallback;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.interfaces.RequestSongCallback;
import jcotter.listenmoe.interfaces.SearchCallback;
import jcotter.listenmoe.interfaces.UserFavoritesCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;

public class MenuActivity extends AppCompatActivity {
    private final String GITHUB_URL = "https://github.com/J-Cotter/LISTEN.moe-Unofficial-Android-App";

    // UI views
    private LinearLayout root;
    private TabHost tabHost;

    // Request Tab
    private TextView req_loginRequired;
    private TextView req_searchText;
    private EditText req_search;
    private Button req_searchButton;
    private ListView req_list;
    private TextView req_remaining;

    // SongsList tab
    private TextView fav_loginRequired;
    private ListView fav_list;

    // Login tab
    private EditText username;
    private EditText password;
    private Button login;
    private Button logout;
    private TextView status;
    private ImageButton github;

    // NON-UI GLOBAL VARIABLES
    private List<Integer> songIds, favorite;
    private List<Boolean> enabled;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        root = (LinearLayout) findViewById(R.id.root);
        tabHost = (TabHost) findViewById(R.id.tabAPI);
        req_loginRequired = (TextView) findViewById(R.id.req_loginRequired);
        req_searchText = (TextView) findViewById(R.id.req_searchText);
        req_search = (EditText) findViewById(R.id.req_search);
        req_searchButton = (Button) findViewById(R.id.req_searchButton);
        req_list = (ListView) findViewById(R.id.req_list);
        req_remaining = (TextView) findViewById(R.id.req_remaining);
        fav_loginRequired = (TextView) findViewById(R.id.fav_loginRequired);
        fav_list = (ListView) findViewById(R.id.fav_list);

        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        login = (Button) findViewById(R.id.login_button);
        logout = (Button) findViewById(R.id.login_logout);
        status = (TextView) findViewById(R.id.loginStatus);
        github = (ImageButton) findViewById(R.id.github);

        // SETUP METHODS
        tabHostSetup();
        tabChangeListener();
        uiClickListeners();
        onWindowFocusChanged(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm.isActive() && getWindow().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        // Removes cursor from the edit text fields
        req_search.clearFocus();
        username.clearFocus();
        password.clearFocus();
    }


    // UI METHODS //

    /**
     * Sets up the tab host.
     */
    private void tabHostSetup() {
        tabHost.setup();
        // Tab 1 //
        TabHost.TabSpec spec = tabHost.newTabSpec(getString(R.string.tabReq));
        spec.setContent(R.id.Requests);
        spec.setIndicator(getString(R.string.tabReq));
        tabHost.addTab(spec);
        // Tab 2 //
        spec = tabHost.newTabSpec(getString(R.string.tabFav));
        spec.setContent(R.id.Favorites);
        spec.setIndicator(getString(R.string.tabFav));
        tabHost.addTab(spec);
        // Tab 3 //
        spec = tabHost.newTabSpec(getString(R.string.tabLogin));
        spec.setContent(R.id.Login);
        spec.setIndicator(getString(R.string.tabLogin));
        tabHost.addTab(spec);
        // Opens Tab specified in intent | Defaults to Request Tab //
        tabHost.setCurrentTab(this.getIntent().getIntExtra("index", 0));
        if (tabHost.getCurrentTab() == 0) {
            requestTab(AuthUtil.getAuthToken(this));
        }
    }

    /**
     * Listener for tab host selection.
     */
    private void tabChangeListener() {
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                // Reset Global Variables //
                adapter = null;
                enabled = null;
                favorite = null;
                songIds = null;
                // Reset UI Components //
                req_search.setText("");
                username.setText("");
                password.setText("");
                if (req_list != null) req_list.setAdapter(null);
                if (fav_list != null) fav_list.setAdapter(null);
                // Changes Tab content if a valid token is available //
                // Not required for Login Tab as same UI Components always shown //
                int currentTab = tabHost.getCurrentTab();
                final String userToken = AuthUtil.getAuthToken(getBaseContext());
                if (currentTab == 0)
                    requestTab(userToken);
                else if (currentTab == 1)
                    favoriteTab(userToken);
                else if (currentTab == 2) {
                    if (userToken != null)
                        status.setVisibility(View.VISIBLE);
                    final long tokenAge = AuthUtil.getTokenAge(getBaseContext());
                    if (tokenAge != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), String.format(getString(R.string.token_age), Math.round((System.currentTimeMillis() / 1000 - tokenAge) / 86400.0)), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Controls content displayed in request tab.
     *
     * @param userToken
     */
    private void requestTab(String userToken) {
        if (userToken == null) {
            req_loginRequired.setVisibility(View.VISIBLE);
            req_searchText.setVisibility(View.GONE);
            req_search.setVisibility(View.GONE);
            req_searchButton.setVisibility(View.GONE);
            req_list.setVisibility(View.GONE);
            req_remaining.setVisibility(View.GONE);
            return;
        }
        req_loginRequired.setVisibility(View.GONE);
        req_searchText.setVisibility(View.VISIBLE);
        req_searchButton.setVisibility(View.VISIBLE);
        req_search.setVisibility(View.VISIBLE);
    }

    /**
     * Controls content displayed in favourite tab.
     *
     * @param userToken
     */
    private void favoriteTab(String userToken) {
        if (userToken == null) {
            fav_loginRequired.setVisibility(View.VISIBLE);
            fav_list.setVisibility(View.GONE);
            return;
        }

        fav_loginRequired.setVisibility(View.GONE);
        fav_list.setVisibility(View.VISIBLE);

        APIUtil.getUserFavorites(this, new UserFavoritesCallback() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final List<Song> favorites) {
                listViewDisplay(favorites, 1);
            }
        });
    }

    /**
     * Processes and displays the relevant listview data.
     *
     * @param songs
     * @param tab
     */
    private void listViewDisplay(final List<Song> songs, final int tab) {
        final int currentTab = tabHost.getCurrentTab();

        List<String> displayList = new ArrayList<>();
        songIds = null;
        enabled = null;
        favorite = null;
        adapter = null;
        songIds = new ArrayList<>();
        favorite = new ArrayList<>();
        enabled = new ArrayList<>();

        // Loop through each song setting whether it is a favorite, enabled, both or neither & Sets song string format
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            displayList.add(song.toString());
            songIds.add(i, song.getId());
            enabled.add(i, song.isEnabled());
            if (currentTab == 0) {
                favorite.add(i, song.isFavorite() ? 1 : 0);
            } else {
                favorite.add(i, 1);
            }
        }

        if (displayList.size() != 0) {
            // Creates a new Adapter using displayList //
            adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, displayList) {
                @SuppressWarnings("deprecation")
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView text = (TextView) super.getView(position, convertView, parent);
                    // Reset view so that it does not keep properties of a recycled view //
                    text.setBackgroundColor(0);
                    text.setTextColor(Color.WHITE);
                    // Sets Text Grey if song is disabled //
                    if (!enabled.get(position)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            text.setTextColor(getColor(R.color.greyText));
                        } else {
                            text.setTextColor(getResources().getColor(R.color.greyText));
                        }
                    }
                    // If current tab is Request Tab sets Pink text for favorites //
                    if (currentTab == 0) {
                        if (favorite.get(position) == 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                text.setBackgroundColor(getColor(R.color.colorAccent));
                            } else {
                                text.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            }
                        }
                    }
                    return text;
                }
            };
        } else {
            // Sets adapter to empty to display nothing
            if (adapter != null) {
                adapter = null;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    if (tab == 0) {
                        req_list.setAdapter(adapter);
                        req_list.setVisibility(View.VISIBLE);
                    } else if (tab == 1) {
                        fav_list.setAdapter(adapter);
                        fav_list.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * Listeners for all clickable UI views.
     */
    private void uiClickListeners() {
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
            }
        });
        req_searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
                if (req_search.getText().toString().trim().length() == 0) return;
                search();
            }
        });
        req_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                confirmationDialog(i);
            }
        });
        fav_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                confirmationDialog(i);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
                if (username.getText().length() == 0)
                    Toast.makeText(getBaseContext(), getString(R.string.errorName), Toast.LENGTH_LONG).show();
                else if (password.getText().length() == 0)
                    Toast.makeText(getBaseContext(), getString(R.string.errorPass), Toast.LENGTH_LONG).show();
                login();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
                logout();
            }
        });
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri link = Uri.parse(GITHUB_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                startActivity(intent);
            }
        });
    }

    /**
     * Displays a dialog containing song actions.
     *
     * @param songIndex
     */
    private void confirmationDialog(final int songIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        // Cancel button
        builder.setMessage(R.string.req_dialog_message);
        builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create button "Favorite"/"Unfavorite"
        final String favoriteAction = favorite.get(songIndex) == 1 ?
                getString(R.string.action_unfavorite) :
                getString(R.string.action_favorite);
        builder.setNegativeButton(favoriteAction, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int in) {
                favorite(songIndex);
            }
        });

        if (enabled.get(songIndex)) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                    request(songIndex);
                }
            });
        }
        builder.create().show();
    }


    // LOGIC METHODS //

    /**
     * Updates the favorite status of a song.
     *
     * @param songIndex
     */
    private void favorite(final int songIndex) {
        final int songID = songIds.get(songIndex);

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
                if (favorited) {
                    favorite.set(songIndex, 1);
                } else {
                    favorite.set(songIndex, 0);

                    // Remove song from favorites list
                    if (tabHost.getCurrentTab() == 1) {
                        songIds.remove(songIndex);
                        favorite.remove(songIndex);
                        enabled.remove(songIndex);
                        adapter.remove(adapter.getItem(songIndex));
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Requests a song.
     *
     * @param songIndex
     */
    private void request(final int songIndex) {
        final int songID = songIds.get(songIndex);

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

                        enabled.set(songIndex, false);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     *
     */
    private void search() {
        final String query = req_search.getText().toString().trim();

        APIUtil.search(this, query, new SearchCallback() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final List<Song> favorites) {
                listViewDisplay(favorites, 0);
            }
        });
    }

    /**
     *
     */
    private void login() {
        final String user = this.username.getText().toString().trim();
        final String pass = this.password.getText().toString().trim();

        APIUtil.authenticate(this, user, pass, new AuthCallback() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String errorMsg = "";

                        switch (result) {
                            case ResponseMessages.INVALID_USER:
                                errorMsg = getString(R.string.errorName);
                                break;
                            case ResponseMessages.INVALID_PASS:
                                errorMsg = getString(R.string.errorPass);
                                break;
                            case ResponseMessages.ERROR:
                                errorMsg = getString(R.string.errorGeneral);
                                break;
                        }

                        Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onSuccess(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setVisibility(View.VISIBLE);
                        Toast.makeText(getBaseContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     *
     */
    private void logout() {
        if (AuthUtil.isAuthenticated(this)) {
            AuthUtil.clearAuthToken(this);
            this.username.setText("");
            this.password.setText("");
            this.status.setVisibility(View.INVISIBLE);
            Toast.makeText(getBaseContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
        }
    }
}
