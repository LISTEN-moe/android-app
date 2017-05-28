package jcotter.listenmoe.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.AuthCallback;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.SDKUtil;

public class MainActivity extends AppCompatActivity {
    // UI views
    private SeekBar mVolumeBar;
    private ImageButton mPlayPauseBtn;
    private ImageButton menuButton;
    private ImageButton mFavoriteBtn;
    private TextView mListenersTxt;
    private TextView mNowPlayingTxt;
    private TextView mRequestedByTxt;

    private AlertDialog mAboutDialog;

    private BroadcastReceiver broadcastReceiver;
    private int songID;
    private boolean favorite;
    private boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up app bar
        setSupportActionBar((Toolbar) findViewById(R.id.appbar));

        // Get UI views
        mPlayPauseBtn = (ImageButton) findViewById(R.id.playPause);
        mVolumeBar = (SeekBar) findViewById(R.id.seekBar);
        mListenersTxt = (TextView) findViewById(R.id.currentText);
        mNowPlayingTxt = (TextView) findViewById(R.id.nowPlaying);
        mRequestedByTxt = (TextView) findViewById(R.id.requestedText);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        mFavoriteBtn = (ImageButton) findViewById(R.id.favoriteButton);

        // Set font to OpenSans
        final Typeface openSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        mListenersTxt.setTypeface(openSans);
        mNowPlayingTxt.setTypeface(openSans);
        mRequestedByTxt.setTypeface(openSans);

        mRequestedByTxt.setVisibility(View.INVISIBLE);

        // Sets audio type to media (volume button control)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Invalidate token if needed
        AuthUtil.checkAuthTokenValidity(this);

        // Set up view listeners
        menuButtonListener();
        volumeSliderListener();
        favoriteButtonListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: use flag to track if registered
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        final Intent intent = new Intent(getBaseContext(), StreamService.class);
        intent.putExtra(StreamService.KILLABLE, true);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        socketDisplay();
        playPauseButtonListener();
    }


    // Overflow menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final boolean authenticated = AuthUtil.isAuthenticated(this);
        menu.findItem(R.id.action_login).setVisible(!authenticated);
        menu.findItem(R.id.action_logout).setVisible(authenticated);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // TODO: replace with search view
                openMenu(0);
                return true;

            case R.id.action_login:
                showLoginDialog();
                return true;

            case R.id.action_logout:
                showLogoutDialog();
                return true;

            case R.id.action_about:
                showAboutDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLoginDialog() {
        final View layout = getLayoutInflater().inflate(R.layout.dialog_login, (ViewGroup) findViewById(R.id.layout_root));
        final EditText mLoginUser = (EditText) layout.findViewById(R.id.login_username);
        final EditText mLoginPass = (EditText) layout.findViewById(R.id.login_password);

        final AlertDialog loginDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.login)
                .setView(layout)
                .setPositiveButton(R.string.login, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Override the positive button listener so it won't automatically be dismissed even with
        // an error
        loginDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String user = mLoginUser.getText().toString().trim();
                        final String pass = mLoginPass.getText().toString().trim();

                        if (user.length() == 0 || pass.length() == 0) {
                            return;
                        }

                        login(user, pass, dialog);
                    }
                });
            }
        });

        loginDialog.show();
    }

    /**
     * Logs the user in with the provided credentials.
     *
     * @param user   Username to pass in the request body.
     * @param pass   Password to pass in the request body.
     * @param dialog Reference to the login dialog so it can be dismissed upon success.
     */
    private void login(final String user, final String pass, final DialogInterface dialog) {
        APIUtil.authenticate(this, user, pass, new AuthCallback() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String errorMsg = "";

                        switch (result) {
                            case ResponseMessages.INVALID_USER:
                                errorMsg = getString(R.string.error_name);
                                break;
                            case ResponseMessages.INVALID_PASS:
                                errorMsg = getString(R.string.error_pass);
                                break;
                            case ResponseMessages.ERROR:
                                errorMsg = getString(R.string.error_general);
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
                        Toast.makeText(getBaseContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        invalidateOptionsMenu();
                    }
                });
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    /**
     * Logs the user out.
     */
    private void logout() {
        if (!AuthUtil.isAuthenticated(this)) {
            return;
        }

        AuthUtil.clearAuthToken(this);
        Toast.makeText(getBaseContext(), getString(R.string.logged_out), Toast.LENGTH_LONG).show();
        invalidateOptionsMenu();
    }

    private void showAboutDialog() {
        if (mAboutDialog == null) {
            String version;
            try {
                version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                version = "";
            }

            mAboutDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                    .setTitle(R.string.about)
                    .setMessage(SDKUtil.fromHtml(getString(R.string.about_content, getString(R.string.app_name), version)))
                    .setPositiveButton(R.string.close, null)
                    .create();
        }

        mAboutDialog.show();

        final TextView textContent = (TextView) mAboutDialog.findViewById(android.R.id.message);
        textContent.setMovementMethod(LinkMovementMethod.getInstance());
    }


    // UI methods
    // TODO: move this stuff into a fragment

    /**
     * Listener for menu button.
     */
    private void menuButtonListener() {
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(0);
            }
        });
    }

    /**
     * Listener for volume slider progress changed.
     */
    private void volumeSliderListener() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mVolumeBar.setProgress((int) (sharedPreferences.getFloat(StreamService.VOLUME, 0.5f) * 100));

        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (StreamService.isServiceRunning) {
                    Intent intent = new Intent(getBaseContext(), StreamService.class)
                            .putExtra(StreamService.VOLUME, seekBar.getProgress() / 100.0f);
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPreferences.edit()
                        .putFloat(StreamService.VOLUME, seekBar.getProgress() / 100.0f);
                editor.apply();
            }
        });
    }

    /**
     * Listener for play/pause button.
     */
    private void playPauseButtonListener() {
        mPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseLogic();
            }
        });
    }

    /**
     * Listener for favorite button.
     */
    private void favoriteButtonListener() {
        mFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoriteLogic();
            }
        });
    }

    /**
     * Displays data received from websocket and checks if stream is playing.
     */
    private void socketDisplay() {
        songID = -1;
        favorite = false;

        broadcastReceiver = new BroadcastReceiver() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceive(Context context, final Intent intent) {
                switch (intent.getAction()) {
                    // Updating current song info from StreamService
                    case StreamService.UPDATE_PLAYING:
                        final StringBuilder playingBuilder = new StringBuilder();
                        final StringBuilder listenersBuilder = new StringBuilder();
                        final StringBuilder requesterBuilder = new StringBuilder();

                        // Fetch data from intent
                        if (intent.hasExtra(StreamService.UPDATE_PLAYING_SONG)) {
                            final Song currentSong = intent.getParcelableExtra(StreamService.UPDATE_PLAYING_SONG);
                            final int listeners = intent.getIntExtra(StreamService.UPDATE_PLAYING_LISTENERS, 0);
                            final String requester = intent.getStringExtra(StreamService.UPDATE_PLAYING_REQUESTER);

                            songID = currentSong.getId();
                            favorite = currentSong.isFavorite();

                            // Current listeners
                            listenersBuilder.append(String.format(getResources().getString(R.string.currentListeners), listeners));

                            // Current song info
                            playingBuilder.append(String.format(getResources().getString(R.string.nowPlaying), currentSong.getTitle(), currentSong.getArtist()));
                            if (!currentSong.getAnime().equals("")) {
                                playingBuilder.append(String.format("\n[ %s ]", currentSong.getAnime()));
                            }

                            // Song requester
                            if (!requester.equals("")) {
                                requesterBuilder.append(String.format(getResources().getString(R.string.requestedText), requester));
                            }
                        } else {
                            playingBuilder.append(getString(R.string.api_failure));
                            listenersBuilder.append(String.format(getResources().getString(R.string.currentListeners), 0));
                        }

                        final String nowPlaying = playingBuilder.toString();
                        final String currentListeners = listenersBuilder.toString();
                        final String requestedBy = requesterBuilder.toString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNowPlayingTxt.setText(nowPlaying);
                                mListenersTxt.setText(currentListeners);

                                if (requestedBy.equals("")) {
                                    mRequestedByTxt.setVisibility(View.INVISIBLE);
                                } else {
                                    mRequestedByTxt.setVisibility(View.VISIBLE);
                                    mRequestedByTxt.setMovementMethod(LinkMovementMethod.getInstance());
                                    mRequestedByTxt.setText(SDKUtil.fromHtml(requestedBy));
                                }
                                final int favDrawable = favorite ? R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp;
                                mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getApplicationContext(), favDrawable));
                            }
                        });
                        break;

                    default:
                        if (intent.hasExtra(StreamService.RUNNING)) {
                            playing = intent.getBooleanExtra(StreamService.RUNNING, false);
                            final int playDrawable = playing ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;
                            mPlayPauseBtn.setImageDrawable(SDKUtil.getDrawable(getApplicationContext(), playDrawable));
                        }

                        if (intent.hasExtra(StreamService.FAVORITE)) {
                            favorite = intent.getBooleanExtra(StreamService.FAVORITE, false);
                            final int favDrawable = favorite ? R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp;
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getApplicationContext(), favDrawable));
                        }
                        break;
                }

            }
        };

        try {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getPackageName());
            intentFilter.addAction(StreamService.UPDATE_PLAYING);
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (IllegalArgumentException ignored) {
        }

        final Intent intent = new Intent(this, StreamService.class);
        if (StreamService.isServiceRunning) {
            intent.putExtra(StreamService.REQUEST, true); // Requests socket update
        } else {
            intent.putExtra(StreamService.RECEIVER, true); // Start service
        }
        intent.putExtra(StreamService.PROBE, true); // Checks if stream is playing
        startService(intent);
    }


    // LOGIC METHODS //

    /**
     * Opens the MenuActivity with the specified tab.
     *
     * @param tabIndex
     */
    private void openMenu(int tabIndex) {
        final Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra(MenuActivity.TAB_INDEX, tabIndex);
        startActivity(intent);
    }

    private void favoriteLogic() {
        if (!AuthUtil.isAuthenticated(this)) {
            openMenu(2);
            return;
        }

        if (songID == -1) return;

        APIUtil.favoriteSong(this, songID, new FavoriteSongCallback() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.equals(ResponseMessages.AUTH_FAILURE)) {
                            Toast.makeText(getBaseContext(), getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            openMenu(2);
                        }
                    }
                });
            }

            @Override
            public void onSuccess(final boolean favorited) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        favorite = favorited;
                        if (favorited) {
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getApplicationContext(), R.drawable.ic_star_black_24dp));
                        } else {
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getApplicationContext(), R.drawable.ic_star_border_black_24dp));
                        }

                        if (StreamService.isServiceRunning) {
                            Intent favUpdate = new Intent(getBaseContext(), StreamService.class)
                                    .putExtra(StreamService.TOGGLE_FAVORITE, favorited);
                            startService(favUpdate);
                        }
                    }
                });
            }
        });

        final Toast toast = Toast.makeText(getBaseContext(), getString(R.string.sending), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 750);
    }

    private void playPauseLogic() {
        if (songID == -1) return;

        final Intent intent = new Intent(this, StreamService.class);
        intent.putExtra(StreamService.PLAY, !playing);
        intent.putExtra(StreamService.VOLUME, mVolumeBar.getProgress() / 100.0f);
        startService(intent);
    }
}