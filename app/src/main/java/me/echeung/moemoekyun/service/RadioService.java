package me.echeung.moemoekyun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import me.echeung.listenmoeapi.RadioSocket;
import me.echeung.listenmoeapi.RadioStream;
import me.echeung.listenmoeapi.callbacks.FavoriteSongCallback;
import me.echeung.listenmoeapi.models.PlaybackInfo;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.UserFragment;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class RadioService extends Service implements RadioSocket.SocketListener {

    private static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final String PLAY_PAUSE = APP_PACKAGE_NAME + ".play_pause";
    public static final String STOP = APP_PACKAGE_NAME + ".stop";
    public static final String TOGGLE_FAVORITE = APP_PACKAGE_NAME + ".toggle_favorite";
    public static final String UPDATE = APP_PACKAGE_NAME + ".update";

    private static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String META_CHANGED = APP_PACKAGE_NAME + ".metachanged";
    public static final String PLAY_STATE_CHANGED = APP_PACKAGE_NAME + ".playstatechanged";

    private final IBinder binder = new ServiceBinder();
    private boolean isServiceBound = false;

    private AppNotification notification;
    private RadioStream stream;
    private RadioSocket socket;

    private MediaSessionCompat mediaSession;

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private boolean wasPlayingBeforeLoss;

    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    @Override
    public IBinder onBind(Intent intent) {
        isServiceBound = true;
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        isServiceBound = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isServiceBound = false;
        if (!isPlaying()) {
            stopSelf();
        }
        return true;
    }

    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioFocusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    stream.setVolume(1f);
                    if (wasPlayingBeforeLoss) {
                        play();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    wasPlayingBeforeLoss = isPlaying();
                    if (wasPlayingBeforeLoss) {
                        pause();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    wasPlayingBeforeLoss = isPlaying();
                    App.getApiClient().getStream().setVolume(0.5f);
                    break;
            }
        };

        initMediaSession();
        initBroadcastReceiver();

        stream = App.getApiClient().getStream();
        socket = App.getApiClient().getSocket();

        App.getApiClient().getSocket().connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        handleIntent(intent);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        socket.disconnect();

        if (receiverRegistered) {
            unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        mediaSession.setActive(false);
        mediaSession.release();

        super.onDestroy();
    }

    public void updateNotification() {
        final Song currentSong = App.getRadioViewModel().getCurrentSong();
        if (currentSong != null && currentSong.getId() != -1) {
            if (notification == null) {
                notification = new AppNotification(this);
            }

            notification.update();
        } else {
            stopForeground(true);
        }
    }

    public boolean isStreamStarted() {
        return stream.isStarted();
    }

    public boolean isPlaying() {
        return stream.isPlaying();
    }

    @Override
    public void onSocketReceive(PlaybackInfo info) {
        final RadioViewModel viewModel = App.getRadioViewModel();

        if (info.getSongId() != 0) {
            viewModel.setCurrentSong(new Song(
                    info.getSongId(),
                    info.getArtistName().trim(),
                    info.getSongName().trim(),
                    info.getAnimeName().trim()
            ));

            viewModel.setLastSong(info.getLast().toString());
            viewModel.setSecondLastSong(info.getSecondLast().toString());

            if (info.hasExtended()) {
                final PlaybackInfo.ExtendedInfo extended = info.getExtended();

                viewModel.setIsFavorited(extended.isFavorite());

                App.getUserViewModel().setQueueSize(extended.getQueue().getSongsInQueue());
                App.getUserViewModel().setQueuePosition(extended.getQueue().getInQueueBeforeUserSong());
            }

            sendPublicIntent(RadioService.META_CHANGED);
        }

        viewModel.setListeners(info.getListeners());
        viewModel.setRequester(info.getRequestedBy());

        updateNotification();
    }

    @Override
    public void onSocketFailure() {
        App.getRadioViewModel().reset();
        updateNotification();
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

                // Pause when headphones unplugged
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    pause();
                    break;

                // Headphone media button action
                case Intent.ACTION_MEDIA_BUTTON:
                    final KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
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
                    if (AuthUtil.isAuthenticated(this)) {
                        socket.update();
                    } else {
                        App.getRadioViewModel().setIsFavorited(false);
                        updateNotification();
                    }
                    break;

                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (NetworkUtil.isNetworkAvailable(this)) {
                        socket.update();
                    }
            }
        }

        updateNotification();
        return true;
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, APP_PACKAGE_NAME, null, null);
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
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return handleIntent(mediaButtonEvent);
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

    private void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        if (!isPlaying()) {
            // Request audio focus for playback
            int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                stream.play();

                App.getRadioViewModel().setIsPlaying(true);
                updateNotification();
                sendPublicIntent(PLAY_STATE_CHANGED);
            }
        }
    }

    private void pause() {
        if (isPlaying()) {
            stream.pause();

            App.getRadioViewModel().setIsPlaying(false);
            updateNotification();
            sendPublicIntent(PLAY_STATE_CHANGED);
        }
    }

    private void stop() {
        if (isPlaying()) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }

        if (isStreamStarted()) {
            stream.stop();
        }

        stopForeground(true);
        stopSelf();

        App.getRadioViewModel().setIsPlaying(false);
        sendPublicIntent(PLAY_STATE_CHANGED);
    }

    private void favoriteCurrentSong() {
        final Song currentSong = App.getRadioViewModel().getCurrentSong();
        if (currentSong == null) return;

        final int songId = currentSong.getId();
        if (songId == -1) return;

        if (!AuthUtil.isAuthenticated(getApplicationContext())) {
            promptLoginFavorite();
            return;
        }

        App.getApiClient().favoriteSong(songId, new FavoriteSongCallback() {
            @Override
            public void onSuccess(final boolean favorited) {
                final Song currentSong = App.getRadioViewModel().getCurrentSong();
                if (currentSong.getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(favorited);
                }

                final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                sendBroadcast(favIntent);

                updateNotification();
            }

            @Override
            public void onFailure(final String message) {
                if (message.equals(Messages.AUTH_FAILURE)) {
                    promptLoginFavorite();
                }
            }
        });
    }

    /**
     * Opens up the login dialog in MainActivity.
     */
    private void promptLoginFavorite() {
        final Intent loginIntent = new Intent(MainActivity.TRIGGER_LOGIN_AND_FAVORITE);
        sendBroadcast(loginIntent);
    }

    /**
     * Sends an intent out for services like Last.fm or Musicxmatch.
     *
     * @param action The broadcast event.
     */
    public void sendPublicIntent(final String action) {
        final Intent intent = new Intent(action.replace(APP_PACKAGE_NAME, MUSIC_PACKAGE_NAME));

        final Song song = App.getRadioViewModel().getCurrentSong();

        intent.putExtra("id", song.getId());

        intent.putExtra("artist", song.getArtist());
        // intent.putExtra("album", song.getAlbum());
        intent.putExtra("track", song.getTitle());

        // TODO: duration/position is needed to work properly with Last.fm and synced Musicxmatch lyrics
        // intent.putExtra("duration", song.duration);
        // intent.putExtra("position", (long) getSongProgressMillis());

        intent.putExtra("playing", isPlaying());

        intent.putExtra("scrobbling_source", APP_PACKAGE_NAME);

        sendStickyBroadcast(intent);
    }

    public class ServiceBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }
}