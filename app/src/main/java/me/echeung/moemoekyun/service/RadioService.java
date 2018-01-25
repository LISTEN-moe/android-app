package me.echeung.moemoekyun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import me.echeung.listenmoeapi.RadioSocket;
import me.echeung.listenmoeapi.RadioStream;
import me.echeung.listenmoeapi.callbacks.FavoriteSongCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.responses.socket.SocketUpdateResponse;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.UserFragment;
import me.echeung.moemoekyun.utils.AlbumArtUtil;
import me.echeung.moemoekyun.utils.ISO8601;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.utils.PreferenceUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class RadioService extends Service implements RadioSocket.SocketListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = RadioService.class.getSimpleName();

    private static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final String PLAY_PAUSE = APP_PACKAGE_NAME + ".play_pause";
    public static final String STOP = APP_PACKAGE_NAME + ".stop";
    public static final String TOGGLE_FAVORITE = APP_PACKAGE_NAME + ".toggle_favorite";
    public static final String UPDATE = APP_PACKAGE_NAME + ".update";
    public static final String TIMER_STOP = APP_PACKAGE_NAME + ".timer_stop";

    private static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String META_CHANGED = APP_PACKAGE_NAME + ".metachanged";
    public static final String PLAY_STATE_CHANGED = APP_PACKAGE_NAME + ".playstatechanged";

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SET_RATING;

    private final IBinder binder = new ServiceBinder();

    private AppNotification notification;
    private RadioStream stream;
    private RadioSocket socket;

    private Calendar trackStartTime;

    private MediaSessionCompat mediaSession;
    private int maxScreenLength;

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private boolean wasPlayingBeforeLoss;

    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    private boolean isFirstConnectivityChange = true;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        maxScreenLength = getMaxScreenLength();

        initBroadcastReceiver();
        initMediaSession();
        initAudioManager();

        stream = App.getApiClient().getStream();
        socket = App.getApiClient().getSocket();

        stream.setListener(new RadioStream.Callback() {
            @Override
            public void onPlay() {
                App.getRadioViewModel().setIsPlaying(true);

                updateNotification();
                updateMediaSessionPlaybackState();

                sendPublicIntent(PLAY_STATE_CHANGED);
            }

            @Override
            public void onPause() {
                App.getRadioViewModel().setIsPlaying(false);

                updateNotification();
                updateMediaSessionPlaybackState();

                sendPublicIntent(PLAY_STATE_CHANGED);
            }

            @Override
            public void onStop() {
                audioManager.abandonAudioFocus(audioFocusChangeListener);

                stopForeground(true);
                stopSelf();

                App.getPreferenceUtil().clearSleepTimer();
                App.getRadioViewModel().setIsPlaying(false);

                updateMediaSessionPlaybackState();

                sendPublicIntent(PLAY_STATE_CHANGED);
            }
        });

        socket.setListener(this);
        socket.connect();

        App.getPreferenceUtil().registerListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        handleIntent(intent);

        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (!isPlaying()) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        stop();
        socket.disconnect();
        stream.removeListener();

        if (receiverRegistered) {
            unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }

        App.getPreferenceUtil().unregisterListener(this);

        super.onDestroy();
    }

    public boolean isStreamStarted() {
        return stream.isStarted();
    }

    public boolean isPlaying() {
        return stream.isPlaying();
    }

    @Override
    public void onSocketReceive(SocketUpdateResponse.Details info) {
        final RadioViewModel viewModel = App.getRadioViewModel();

        final Song song = info.getSong();
        viewModel.setCurrentSong(song);

        try {
            this.trackStartTime = ISO8601.toCalendar(info.getStartTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        viewModel.setLastSong(info.getLastPlayed().get(0));
        viewModel.setSecondLastSong(info.getLastPlayed().get(1));

        viewModel.setListeners(info.getListeners());
        viewModel.setRequester(info.getRequester());
        viewModel.setEvent(info.getEvent());

        if (info.getQueue() != null) {
            viewModel.setQueueSize(info.getQueue().getInQueue());
            viewModel.setInQueueByUser(info.getQueue().getInQueueByUser());
            viewModel.setQueuePosition(info.getQueue().getInQueueBeforeUser());
        }

        updateMediaSession();
        updateNotification();

        sendPublicIntent(RadioService.META_CHANGED);
    }

    @Override
    public void onSocketFailure() {
        App.getRadioViewModel().reset();
        updateNotification();
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    private void updateMediaSession() {
        final Song currentSong = App.getRadioViewModel().getCurrentSong();

        if (currentSong == null) {
            mediaSession.setMetadata(null);
            return;
        }

        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.getArtistString())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.getAlbumString())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentSong.getDuration());

        if (App.getPreferenceUtil().shouldShowLockscreenAlbumArt()) {
            AlbumArtUtil.getAlbumArtBitmap(this, currentSong, maxScreenLength, bitmap -> {
                metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                updateMediaSession(metaData);
            });
        }

        updateMediaSession(metaData);
    }

    private void updateMediaSession(MediaMetadataCompat.Builder metaData) {
        mediaSession.setMetadata(metaData.build());
        updateMediaSessionPlaybackState();
    }

    private void updateMediaSessionPlaybackState() {
        // Play/pause state
        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(isStreamStarted()
                                ? isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED
                                : PlaybackStateCompat.STATE_STOPPED,
                        0, 1);

        // Favorite action
        if (App.getAuthUtil().isAuthenticated()) {
            final Song currentSong = App.getRadioViewModel().getCurrentSong();
            final int favoriteIcon = currentSong == null || !currentSong.isFavorite() ?
                    R.drawable.ic_star_border_white_24dp :
                    R.drawable.ic_star_white_24dp;

            stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                    TOGGLE_FAVORITE, getString(R.string.favorite), favoriteIcon)
                    .build());
        }

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateNotification() {
        if (isStreamStarted()) {
            if (notification == null) {
                notification = new AppNotification(this);
            }

            notification.update();
        } else {
            stopForeground(true);
        }
    }

    private boolean handleIntent(Intent intent) {
        if (intent == null) return true;

        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case RadioService.PLAY_PAUSE:
                    togglePlayPause();
                    break;

                case RadioService.STOP:
                    stop();
                    break;

                case RadioService.TOGGLE_FAVORITE:
                    favoriteCurrentSong();
                    break;

                case RadioService.UPDATE:
                    socket.update();
                    break;

                case RadioService.TIMER_STOP:
                    timerStop();
                    break;

                // Pause when headphones unplugged
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    if (App.getPreferenceUtil().shouldPauseOnNoisy()) {
                        pause();
                    }
                    break;

                // Headphone media button action
                case Intent.ACTION_MEDIA_BUTTON:
                    final Bundle extras = intent.getExtras();
                    if (extras == null) {
                        return false;
                    }

                    final KeyEvent keyEvent = (KeyEvent) extras.get(Intent.EXTRA_KEY_EVENT);
                    if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                        return false;
                    }

                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            togglePlayPause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            play();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            stop();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            // Do nothing
                            break;
                    }
                    break;

                case MainActivity.AUTH_EVENT:
                    socket.reconnect();
                    if (!App.getAuthUtil().isAuthenticated()) {
                        App.getRadioViewModel().setIsFavorited(false);
                        updateNotification();
                    }
                    break;

                case ConnectivityManager.CONNECTIVITY_ACTION:
                    // Ignore the initial sticky broadcast on app start
                    if (isFirstConnectivityChange) {
                        isFirstConnectivityChange = false;
                        break;
                    }

                    if (NetworkUtil.isNetworkAvailable(this)) {
                        socket.connect();
                    } else {
                        socket.disconnect();
                    }
            }
        }

        updateNotification();
        return true;
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, APP_PACKAGE_NAME, null, null);
        mediaSession.setRatingType(RatingCompat.RATING_HEART);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onStop() {
                stop();
            }

            @Override
            public void onSkipToNext() {
            }

            @Override
            public void onSkipToPrevious() {
            }

            @Override
            public void onSeekTo(long pos) {
            }

            @Override
            public void onSetRating(RatingCompat rating) {
                favoriteCurrentSong();
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return handleIntent(mediaButtonEvent);
            }

            @Override
            public void onCustomAction(@NonNull String action, Bundle extras) {
                switch (action) {
                    case TOGGLE_FAVORITE:
                        favoriteCurrentSong();
                        updateMediaSessionPlaybackState();
                        break;

                    default:
                        Log.d(TAG, "Unsupported action: " + action);
                        break;
                }
            }

            @Override
            public void onPlayFromSearch(String query, Bundle extras) {
                // We don't support searching for specific things since it's just a radio stream
                // so just toggle playback
                togglePlayPause();
            }
        });

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);

        mediaSession.setActive(true);
    }

    private void initBroadcastReceiver() {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RadioService.PLAY_PAUSE);
        intentFilter.addAction(RadioService.STOP);
        intentFilter.addAction(RadioService.TOGGLE_FAVORITE);
        intentFilter.addAction(RadioService.UPDATE);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
    }

    private void initAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioFocusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    stream.unduck();
                    if (wasPlayingBeforeLoss) {
                        play();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    wasPlayingBeforeLoss = isPlaying();
                    if (wasPlayingBeforeLoss && App.getPreferenceUtil().shouldPauseAudioOnLoss()) {
                        pause();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    wasPlayingBeforeLoss = isPlaying();
                    if (App.getPreferenceUtil().shouldDuckAudio()) {
                        stream.duck();
                    }
                    break;
            }
        };
    }

    private void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            stream.play();
        }
    }

    private void pause() {
        stream.pause();
    }

    private void stop() {
        stream.stop();
    }

    private void timerStop() {
        stream.fadeOut();
    }

    private void favoriteCurrentSong() {
        final Song currentSong = App.getRadioViewModel().getCurrentSong();
        if (currentSong == null) return;

        final int songId = currentSong.getId();
        if (songId == -1) return;

        if (!App.getAuthUtil().isAuthenticated()) {
            showLoginRequiredToast();
            return;
        }

        final boolean isCurrentlyFavorite = currentSong.isFavorite();

        final FavoriteSongCallback callback = new FavoriteSongCallback() {
            @Override
            public void onSuccess() {
                final Song currentSong = App.getRadioViewModel().getCurrentSong();
                if (currentSong.getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(!isCurrentlyFavorite);
                }

                final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                sendBroadcast(favIntent);

                updateNotification();
                updateMediaSessionPlaybackState();
            }

            @Override
            public void onFailure(final String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        };

        App.getApiClient().toggleFavorite(String.valueOf(songId), isCurrentlyFavorite, callback);
    }

    private void showLoginRequiredToast() {
        Toast.makeText(getApplicationContext(), R.string.login_required, Toast.LENGTH_SHORT).show();
    }

    /**
     * Sends an intent out for services like Last.fm.
     *
     * @param action The broadcast event.
     */
    public void sendPublicIntent(final String action) {
        final Song song = App.getRadioViewModel().getCurrentSong();
        if (song == null || !App.getPreferenceUtil().shouldBroadcastIntent()) return;

        // Scrobbling only works if there's actually progress
        if (song.getDuration() == 0 || trackStartTime == null) return;

        final Intent intent = new Intent(action.replace(APP_PACKAGE_NAME, MUSIC_PACKAGE_NAME));

        intent.putExtra("id", song.getId());

        intent.putExtra("artist", song.getAlbumString());
        intent.putExtra("album", song.getAlbumString());
        intent.putExtra("track", song.getTitle());

        intent.putExtra("duration", song.getDuration());
        intent.putExtra("position", GregorianCalendar.getInstance().getTimeInMillis() - trackStartTime.getTimeInMillis());

        intent.putExtra("playing", isPlaying());

        intent.putExtra("scrobbling_source", APP_PACKAGE_NAME);

        sendStickyBroadcast(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.PREF_LOCKSCREEN_ALBUMART:
                updateMediaSession();
                break;
        }
    }

    private int getMaxScreenLength() {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public class ServiceBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }

}
