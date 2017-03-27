package jcotter.listenmoe.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.R;
import jcotter.listenmoe.interfaces.APIListenerInterface;

public class RadioActivity extends AppCompatActivity {

    // [GLOBAL VARIABLES] //
    // UI VARIABLES //
    ImageView background;
    SeekBar volumeSlider;
    ImageButton playPause;
    ImageButton menuButton;
    ImageButton favoriteButton;
    TextView poweredBy;
    TextView currentText;
    TextView nowPlaying;
    TextView requestText;
    // NON-UI GLOBAL VARIABLES
    String userToken;
    BroadcastReceiver broadcastReceiver;
    int songID;
    boolean favorite;
    boolean playing;

    // [METHODS] //
    // SYSTEM METHODS //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        // Get UI Components //
        playPause = (ImageButton) findViewById(R.id.playPause);
        volumeSlider = (SeekBar) findViewById(R.id.seekBar);
        poweredBy = (TextView) findViewById(R.id.poweredBy);
        currentText = (TextView) findViewById(R.id.currentText);
        nowPlaying = (TextView) findViewById(R.id.nowPlaying);
        requestText = (TextView) findViewById(R.id.requestedText);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
        // Set Font To OpenSans //
        Typeface openSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        poweredBy.setTypeface(openSans);
        currentText.setTypeface(openSans);
        nowPlaying.setTypeface(openSans);
        requestText.setTypeface(openSans);

        requestText.setVisibility(View.INVISIBLE);

        // Sets Audio Type To Media (Volume Button Control) //
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // SETUP METHODS //
        menuButtonListener();
        volumeSliderListener();
        favoriteButtonListener();
        getToken();
    }

    @Override
    public void onPause() {
        super.onPause();
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
    private void menuButtonListener() {
        // PURPOSE: LISTENER FOR MENU BUTTON //
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(0);
            }
        });
    }

    private void volumeSliderListener() {
        // PURPOSE: LISTENER FOR VOLUME SLIDER PROGRESS CHANGED //
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

    private void playPauseButtonListener() {
        // PURPOSE: LISTENER FOR PLAY/PAUSE BUTTON //
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseLogic();
            }
        });
    }

    private void favoriteButtonListener() {
        // PURPOSE: LISTENER FOR FAVORITE BUTTON //
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoriteLogic();
            }
        });
    }

    private void socketDisplay() {
        // PURPOSE: DISPLAYS DATA RECEIVED FROM WEBSOCKET | CHECKS IF STREAM IS PLAYING //
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
                            nowPlaying.setText(intent.getStringExtra("nowPlaying"));
                            currentText.setText(intent.getStringExtra("listeners"));
                            if (!intent.getStringExtra("requestedBy").equals("NULL")) {
                                requestText.setVisibility(View.VISIBLE);
                                requestText.setMovementMethod(LinkMovementMethod.getInstance());
                                if (Build.VERSION.SDK_INT >= 24) {
                                    requestText.setText(Html.fromHtml(intent.getStringExtra("requestedBy"), Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    requestText.setText(Html.fromHtml(intent.getStringExtra("requestedBy")));
                                }
                            } else
                                requestText.setVisibility(View.INVISIBLE);
                            songID = intent.getIntExtra("songID", -1);
                            favorite = intent.getBooleanExtra("favorite", false);
                            if (Build.VERSION.SDK_INT >= 21) {
                                if (favorite)
                                    favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_full));
                                else
                                    favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_empty));
                            } else {
                                if (favorite)
                                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                                else
                                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                            }
                        }
                    });
                }
                if (intent.hasExtra("running")) {
                    if (intent.getBooleanExtra("running", false)) {
                        playing = true;
                        if (Build.VERSION.SDK_INT >= 21) {
                            playPause.setImageDrawable(getDrawable(R.drawable.icon_pause));
                        } else {
                            playPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
                        }
                    } else {
                        playing = false;
                        if (Build.VERSION.SDK_INT >= 21) {
                            playPause.setImageDrawable(getDrawable(R.drawable.icon_play));
                        } else {
                            playPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
                        }
                    }
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
        } else
            intent.putExtra("receiver", true); // Start StreamService //
        startService(intent);
    }

    // LOGIC METHODS //
    private void getToken() {
        // PURPOSE : NEW TOKEN IF CURRENT > 20 DAYS OLD  OTHERWISE RETURN CURRENT TOKEN //
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Returns if current token < 20 days old //
        if (!sharedPreferences.getString("userToken", "NULL").equals("NULL")) {
            userToken = sharedPreferences.getString("userToken", "NULL");
        }
    }

    private void openMenu(int tabIndex) {
        // PURPOSE: OPENS MENU.CLASS AND SPECIFIES WHICH TAB TO DISPLAY //
        Intent intent = new Intent(this, MenuActivity.class)
                .putExtra("index", tabIndex);
        startActivity(intent);
    }

    private void favoriteLogic() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getString("userToken", "NULL").equals("NULL")) {
            openMenu(2);
            return;
        }
        if (songID == -1) return;
        APIUtil apiUtil = new APIUtil(new APIListenerInterface() {
            @Override
            public void favoriteCallback(final String jsonResult) {
                runOnUiThread(new Runnable() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void run() {
                        if (jsonResult.contains("success\":true")) {
                            if (jsonResult.contains("favorite\":true")) {
                                favorite = true;
                                if (Build.VERSION.SDK_INT >= 21) {
                                    favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_full));
                                } else {
                                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_full));
                                }
                            } else {
                                favorite = false;
                                if (Build.VERSION.SDK_INT >= 21) {
                                    favoriteButton.setImageDrawable(getDrawable(R.drawable.favorite_empty));
                                } else {
                                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.favorite_empty));
                                }
                            }
                        } else if (jsonResult.contains("Failed to authenticate token.")) {
                            Toast.makeText(getBaseContext(), "Token Expired", Toast.LENGTH_SHORT).show();
                            openMenu(2);
                        }
                    }
                });
            }

            @Override
            public void favoriteListCallback(String jsonResult) {
            }

            @Override
            public void authenticateCallback(String token) {
            }

            @Override
            public void requestCallback(String jsonResult) {
            }

            @Override
            public void searchCallback(String jsonResult) {
            }
        });
        apiUtil.favorite(songID, getApplicationContext());

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

    private boolean isRunning() {
        // PURPOSE: CHECKS IF SOCKET STREAM SERVICE IS RUNNING //
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}