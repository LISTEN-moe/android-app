package me.echeung.moemoekyun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;

import androidx.annotation.NonNull;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback;
import me.echeung.moemoekyun.client.api.library.Jpop;
import me.echeung.moemoekyun.client.api.library.Kpop;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.client.socket.Socket;
import me.echeung.moemoekyun.client.socket.response.UpdateResponse;
import me.echeung.moemoekyun.client.stream.Stream;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.util.AlbumArtUtil;
import me.echeung.moemoekyun.util.PreferenceUtil;
import me.echeung.moemoekyun.util.SongActionsUtil;
import me.echeung.moemoekyun.util.system.TimeUtil;
import me.echeung.moemoekyun.viewmodel.RadioViewModel;

public class RadioService extends Service implements Socket.Listener, AlbumArtUtil.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = RadioService.class.getSimpleName();

    private static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    private static final int MILLISECONDS_IN_SECOND = 1000;

    public static final String PLAY_PAUSE = APP_PACKAGE_NAME + ".play_pause";
    public static final String STOP = APP_PACKAGE_NAME + ".stop";
    public static final String TOGGLE_FAVORITE = APP_PACKAGE_NAME + ".toggle_favorite";
    public static final String LIBRARY_JPOP = APP_PACKAGE_NAME + ".library_jpop";
    public static final String LIBRARY_KPOP = APP_PACKAGE_NAME + ".library_kpop";
    public static final String UPDATE = APP_PACKAGE_NAME + ".update";
    public static final String TIMER_STOP = APP_PACKAGE_NAME + ".timer_stop";

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SET_RATING;

    private final IBinder binder = new ServiceBinder();

    private AppNotification notification;
    private Stream stream;
    private Socket socket;

    private volatile MediaSessionCompat mediaSession;
    private final Object mediaSessionLock = new Object();

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
        AlbumArtUtil.registerListener(this);

        initBroadcastReceiver();
        initMediaSession();
        initAudioManager();

        stream = App.getRadioClient().getStream();
        socket = App.getRadioClient().getSocket();

        stream.setListener(new Stream.Listener() {
            @Override
            public void onStreamPlay() {
                App.getRadioViewModel().setIsPlaying(true);

                updateNotification();
                updateMediaSessionPlaybackState();
            }

            @Override
            public void onStreamPause() {
                App.getRadioViewModel().setIsPlaying(false);

                updateNotification();
                updateMediaSessionPlaybackState();
            }

            @Override
            public void onStreamStop() {
                audioManager.abandonAudioFocus(audioFocusChangeListener);

                stopForeground(true);
                stopSelf();

                App.getPreferenceUtil().clearSleepTimer();
                App.getRadioViewModel().setIsPlaying(false);

                updateMediaSessionPlaybackState();
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
        AlbumArtUtil.unregisterListener(this);

        stop();
        socket.disconnect();
        stream.removeListener();

        if (receiverRegistered) {
            unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        destroyMediaSession();

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
    public void onSocketReceive(UpdateResponse.Details info) {
        RadioViewModel viewModel = App.getRadioViewModel();

        viewModel.setListeners(info.getListeners());
        viewModel.setRequester(info.getRequester());
        viewModel.setEvent(info.getEvent());

        if (info.getQueue() != null) {
            viewModel.setQueueSize(info.getQueue().getInQueue());
            viewModel.setInQueueByUser(info.getQueue().getInQueueByUser());
            viewModel.setQueuePosition(info.getQueue().getInQueueBeforeUser());
        }

        Calendar startTime = null;
        try {
            startTime = TimeUtil.toCalendar(info.getStartTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        viewModel.setCurrentSong(info.getSong(), startTime);
        viewModel.setLastSong(info.getLastPlayed().get(0));
        viewModel.setSecondLastSong(info.getLastPlayed().get(1));

        updateMediaSession();
        updateNotification();
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
        Song currentSong = App.getRadioViewModel().getCurrentSong();

        if (currentSong == null) {
            mediaSession.setMetadata(null);
            return;
        }

        MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getTitleString())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.getArtistsString())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.getAlbumsString())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentSong.getDuration() * MILLISECONDS_IN_SECOND);

        if (App.getPreferenceUtil().shouldShowLockscreenAlbumArt()) {
            Bitmap albumArt = AlbumArtUtil.getCurrentAlbumArt();
            if (albumArt != null && !AlbumArtUtil.isDefaultAlbumArt()) {
                metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
                updateNotification();
            }
        }

        synchronized (mediaSessionLock) {
            mediaSession.setMetadata(metaData.build());
            updateMediaSessionPlaybackState();
        }
    }

    private void updateMediaSessionPlaybackState() {
        // Play/pause state
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(isStreamStarted()
                                ? isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED
                                : PlaybackStateCompat.STATE_STOPPED,
                        App.getRadioViewModel().getCurrentSongProgress(), 1);

        // Favorite action
        if (App.getAuthUtil().isAuthenticated()) {
            Song currentSong = App.getRadioViewModel().getCurrentSong();
            int favoriteIcon = currentSong == null || !currentSong.isFavorite() ?
                    R.drawable.ic_star_border_white_24dp :
                    R.drawable.ic_star_white_24dp;

            stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                    TOGGLE_FAVORITE, getString(R.string.favorite), favoriteIcon)
                    .build());
        }

        if (mediaSession.isActive()) {
            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    @Override
    public void onAlbumArtReady(Bitmap bitmap) {
        updateMediaSession();
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

        String action = intent.getAction();
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
                case SongActionsUtil.REQUEST_EVENT:
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
                    Bundle extras = intent.getExtras();
                    if (extras == null) {
                        return false;
                    }

                    KeyEvent keyEvent = (KeyEvent) extras.get(Intent.EXTRA_KEY_EVENT);
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

                    socket.reconnect();
            }
        }

        updateNotification();
        return true;
    }

    private void initMediaSession() {
        synchronized (mediaSessionLock) {
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

                @Override
                public void onPlayFromMediaId(String mediaId, Bundle extras) {
                    super.onPlayFromMediaId(mediaId, extras);

                    // Handles changing library mode via Android Auto
                    switch (mediaId) {
                        case LIBRARY_JPOP:
                            App.getRadioClient().changeLibrary(Jpop.NAME);
                            break;

                        case LIBRARY_KPOP:
                            App.getRadioClient().changeLibrary(Kpop.NAME);
                            break;
                    }
                }
            });

            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

            mediaSession.setActive(true);
        }
    }

    private void destroyMediaSession() {
        synchronized (mediaSessionLock) {
            if (mediaSession != null) {
                mediaSession.setActive(false);
                mediaSession.release();
            }
        }
    }

    private void initBroadcastReceiver() {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RadioService.PLAY_PAUSE);
        intentFilter.addAction(RadioService.STOP);
        intentFilter.addAction(RadioService.TOGGLE_FAVORITE);
        intentFilter.addAction(RadioService.UPDATE);
        intentFilter.addAction(SongActionsUtil.REQUEST_EVENT);
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
                    if (wasPlayingBeforeLoss &&
                            (App.getPreferenceUtil().shouldPauseAudioOnLoss()
                                    || AutoMediaBrowserService.isCarUiMode(this))) {
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
        Song currentSong = App.getRadioViewModel().getCurrentSong();
        if (currentSong == null) return;

        int songId = currentSong.getId();
        if (songId == -1) return;

        if (!App.getAuthUtil().isAuthenticated()) {
            showLoginRequiredToast();
            return;
        }

        boolean isCurrentlyFavorite = currentSong.isFavorite();

        FavoriteSongCallback callback = new FavoriteSongCallback() {
            @Override
            public void onSuccess() {
                Song currentSong = App.getRadioViewModel().getCurrentSong();
                if (currentSong.getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(!isCurrentlyFavorite);
                }

                Intent favIntent = new Intent(SongActionsUtil.FAVORITE_EVENT);
                sendBroadcast(favIntent);

                updateNotification();
                updateMediaSessionPlaybackState();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        };

        App.getRadioClient().getApi().toggleFavorite(String.valueOf(songId), isCurrentlyFavorite, callback);
    }

    private void showLoginRequiredToast() {
        Toast.makeText(getApplicationContext(), R.string.login_required, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.PREF_LOCKSCREEN_ALBUMART:
            case PreferenceUtil.PREF_GENERAL_ROMAJI:
                updateMediaSession();
                break;
        }
    }

    public class ServiceBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }

}
