package me.echeung.moemoekyun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import me.echeung.listenmoeapi.RadioSocket;
import me.echeung.listenmoeapi.RadioStream;
import me.echeung.listenmoeapi.callbacks.FavoriteSongCallback;
import me.echeung.listenmoeapi.models.PlaybackInfo;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.UserFragment;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class RadioService extends Service implements RadioSocket.SocketListener {

    private static final String TAG = RadioService.class.getSimpleName();

    private static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final String PLAY_PAUSE = APP_PACKAGE_NAME + ".play_pause";
    public static final String STOP = APP_PACKAGE_NAME + ".stop";
    public static final String TOGGLE_FAVORITE = APP_PACKAGE_NAME + ".toggle_favorite";
    public static final String UPDATE = APP_PACKAGE_NAME + ".update";

    private static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String META_CHANGED = APP_PACKAGE_NAME + ".metachanged";
    public static final String PLAY_STATE_CHANGED = APP_PACKAGE_NAME + ".playstatechanged";

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_STOP;

    private final IBinder binder = new ServiceBinder();

    private AppNotification notification;
    private RadioStream stream;
    private RadioSocket socket;

    private MediaSessionCompat mediaSession;

    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    private Bitmap background;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        initMediaSession();
        initBroadcastReceiver();

        stream = App.getApiClient().getStream();
        socket = App.getApiClient().getSocket();

        socket.connect();

        // Preload background image for media session
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
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

        if (receiverRegistered) {
            unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }

        super.onDestroy();
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
                    info.getArtistName(),
                    info.getSongName(),
                    info.getAnimeName()
            ));

            viewModel.setLastSong(info.getLast().toString());
            viewModel.setSecondLastSong(info.getSecondLast().toString());

            if (info.hasExtended()) {
                final PlaybackInfo.ExtendedInfo extended = info.getExtended();

                viewModel.setIsFavorited(extended.isFavorite());

                App.getUserViewModel().setQueueSize(extended.getQueue().getSongsInQueue());
                App.getUserViewModel().setQueuePosition(extended.getQueue().getInQueueBeforeUserSong());
            }
        }

        viewModel.setListeners(info.getListeners());
        viewModel.setRequester(info.getRequestedBy());

        updateMediaSession(info);
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

    private void updateMediaSession(PlaybackInfo info) {
        if (info.getSongId() == 0) {
            mediaSession.setMetadata(null);
            return;
        }

        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getSongName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, info.getAnimeName())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, background);

        mediaSession.setMetadata(metaData.build());

        updateMediaSessionPlaybackState();
    }

    private void updateMediaSessionPlaybackState() {
        // Play/pause state
        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(MEDIA_SESSION_ACTIONS)
            .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, 0, 1);

        // Favorite action
        if (AuthUtil.isAuthenticated(this)) {
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

    private void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        if (stream.play()) {
            App.getRadioViewModel().setIsPlaying(true);

            updateNotification();
            updateMediaSessionPlaybackState();

            sendPublicIntent(PLAY_STATE_CHANGED);
        }
    }

    private void pause() {
        if (stream.pause()) {
            App.getRadioViewModel().setIsPlaying(false);

            updateNotification();
            updateMediaSessionPlaybackState();

            sendPublicIntent(PLAY_STATE_CHANGED);
        }
    }

    private void stop() {
        if (stream.stop()) {
            stopForeground(true);
            stopSelf();

            App.getRadioViewModel().setIsPlaying(false);

            updateMediaSessionPlaybackState();

            sendPublicIntent(PLAY_STATE_CHANGED);
        }
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
        final Song song = App.getRadioViewModel().getCurrentSong();
        if (song == null) return;

        final Intent intent = new Intent(action.replace(APP_PACKAGE_NAME, MUSIC_PACKAGE_NAME));

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