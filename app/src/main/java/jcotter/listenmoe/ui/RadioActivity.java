package jcotter.listenmoe.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import jcotter.listenmoe.R;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;

public class RadioActivity extends AppCompatActivity {
    // UI views
    private SeekBar volumeSlider;
    private ImageButton playPause;
    private ImageButton menuButton;
    private ImageButton favoriteButton;
    private TextView poweredBy;
    private TextView currentText;
    private TextView nowPlaying;
    private TextView requestText;

    private BroadcastReceiver broadcastReceiver;
    private int songID;
    private boolean favorite;
    private boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        // Get UI views
        playPause = (ImageButton) findViewById(R.id.playPause);
        volumeSlider = (SeekBar) findViewById(R.id.seekBar);
        poweredBy = (TextView) findViewById(R.id.poweredBy);
        currentText = (TextView) findViewById(R.id.currentText);
        nowPlaying = (TextView) findViewById(R.id.nowPlaying);
        requestText = (TextView) findViewById(R.id.requestedText);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);

        // Set font to OpenSans
        Typeface openSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        poweredBy.setTypeface(openSans);
        currentText.setTypeface(openSans);
        nowPlaying.setTypeface(openSans);
        requestText.setTypeface(openSans);

        requestText.setVisibility(View.INVISIBLE);

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
        // Purpose: Allows Stream to
        super.onStop();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        Intent intent = new Intent(getBaseContext(), StreamService.class)
                .putExtra("killable", true);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        socketDisplay();
        playPauseButtonListener();
    }


    // UI METHODS //

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
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isRunning()) {
                    Intent intent = new Intent(getBaseContext(), StreamService.class)
                            .putExtra("volume", seekBar.getProgress() / 100.0f);
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Listener for play/pause button.
     */
    private void playPauseButtonListener() {
        playPause.setOnClickListener(new View.OnClickListener() {
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
        favoriteButton.setOnClickListener(new View.OnClickListener() {
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
                if (intent.hasExtra("nowPlaying")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            songID = intent.getIntExtra("songID", -1);
                            nowPlaying.setText(intent.getStringExtra("nowPlaying"));
                            currentText.setText(intent.getStringExtra("listeners"));
                            final String requestedBy = intent.getStringExtra("requestedBy");
                            if (requestedBy != null) {
                                requestText.setVisibility(View.VISIBLE);
                                requestText.setMovementMethod(LinkMovementMethod.getInstance());
                                if (Build.VERSION.SDK_INT >= 24)
                                    requestText.setText(Html.fromHtml(requestedBy, Html.FROM_HTML_MODE_COMPACT));
                                else
                                    requestText.setText(Html.fromHtml(requestedBy));
                            } else {
                                requestText.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
                if (intent.hasExtra("running")) {
                    if (intent.getBooleanExtra("running", false)) {
                        playing = true;
                        if (Build.VERSION.SDK_INT >= 21)
                            playPause.setImageDrawable(getDrawable(R.drawable.icon_pause));
                        else
                            playPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
                    } else {
                        playing = false;
                        if (Build.VERSION.SDK_INT >= 21)
                            playPause.setImageDrawable(getDrawable(R.drawable.icon_play));
                        else
                            playPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
                    }
                }
                if (intent.hasExtra("favorite")) {
                    favorite = intent.getBooleanExtra("favorite", false);
                    if (Build.VERSION.SDK_INT >= 21)
                        if (favorite)
                            favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_full));
                        else
                            favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_empty));
                    else if (favorite)
                        favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                    else
                        favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                }
                if (intent.hasExtra("volume"))
                    volumeSlider.setProgress(intent.getIntExtra("volume", 50));
            }
        };

        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("jcotter.listenmoe");
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (IllegalArgumentException ignored) {
        }
        Intent intent = new Intent(this, StreamService.class);
        if (isRunning()) {
            intent.putExtra("re:re", true); // Requests Socket Update //
            intent.putExtra("probe", true); // Checks if Music Stream is Playing //
        } else {
            intent.putExtra("receiver", true); // Start StreamService //
            intent.putExtra("probe", true); // Get Volume & Checks if Music Stream is Playing //
        }
        startService(intent);
    }


    // LOGIC METHODS //

    /**
     * Opens the MenuActivity with the specified tab.
     *
     * @param tabIndex
     */
    private void openMenu(int tabIndex) {
        Intent intent = new Intent(this, MenuActivity.class)
                .putExtra("index", tabIndex);
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
            }

            @Override
            public void onSuccess(final String jsonResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (jsonResult.contains("success\":true")) {
                            if (jsonResult.contains("favorite\":true")) {
                                favorite = true;
                                if (Build.VERSION.SDK_INT >= 21)
                                    favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_full));
                                else
                                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                            } else {
                                favorite = false;
                                if (Build.VERSION.SDK_INT >= 21)
                                    favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_empty));
                                else
                                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                            }
                        } else if (jsonResult.contains("Failed to authenticate token.")) {
                            Toast.makeText(getBaseContext(), "Token Expired", Toast.LENGTH_SHORT).show();
                            openMenu(2);
                        }
                        if (isRunning()) {
                            Intent favUpdate = new Intent(getBaseContext(), StreamService.class)
                                    .putExtra("favUpdate", true);
                            startService(favUpdate);
                        }
                    }
                });
            }
        });

        final Toast toast = Toast.makeText(getBaseContext(), "Sending...", Toast.LENGTH_SHORT);
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
        Intent intent = new Intent(this, StreamService.class);
        if (playing)
            intent.putExtra("play", false);
        else {
            intent.putExtra("play", true);
            intent.putExtra("volume", volumeSlider.getProgress() / 100.0f);
        }
        startService(intent);
    }

    /**
     * Checks if socket stream service is running.
     *
     * @return Whether the service is running.
     */
    private boolean isRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}