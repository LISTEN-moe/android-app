package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.api.models.Event;
import me.echeung.moemoekyun.api.models.Song;
import me.echeung.moemoekyun.api.models.User;
import me.echeung.moemoekyun.utils.AlbumArtUtil;
import me.echeung.moemoekyun.utils.PreferenceUtil;
import me.echeung.moemoekyun.utils.ThemeUtil;

public class RadioViewModel extends BaseViewModel implements AlbumArtUtil.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    private Song currentSong;

    private boolean isPlaying;
    private int listeners;
    private User requester;
    private Event event;

    private Song lastSong;
    private Song secondLastSong;

    private int queueSize;
    private int inQueueByUser;
    private int queuePosition;

    private float miniPlayerAlpha;

    public RadioViewModel(Context context) {
        super(context);

        // Defaults
        isPlaying = false;

        AlbumArtUtil.registerListener(this);
        App.getPreferenceUtil().registerListener(this);
    }

    public void reset() {
        setCurrentSong(null);

        setListeners(0);
        setRequester(null);
        setEvent(null);

        setLastSong(null);
        setSecondLastSong(null);

        setQueueSize(0);
        setInQueueByUser(0);
        setQueuePosition(0);
    }


    // Play state
    // ========================================================================

    @Bindable
    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;

        setIsFavorited(currentSong != null && currentSong.isFavorite());

        AlbumArtUtil.updateAlbumArt(getContext(), currentSong);

        notifyPropertyChanged(BR.currentSong);
    }

    @Bindable
    public Bitmap getAlbumArt() {
        return AlbumArtUtil.getCurrentAlbumArt();
    }

    @Bindable
    @ColorInt
    public int getBackgroundColor() {
        if (App.getPreferenceUtil().shouldColorNowPlaying() && !AlbumArtUtil.isDefaultAlbumArt()) {
            final int accentColor = AlbumArtUtil.getCurrentAccentColor();
            if (accentColor != 0) {
                return accentColor;
            }
        }

        return ThemeUtil.getBackgroundColor(getContext());
    }

    @Bindable
    @ColorInt
    public int getBodyColor() {
        if (App.getPreferenceUtil().shouldColorNowPlaying() && !AlbumArtUtil.isDefaultAlbumArt()) {
            final int bodyColor = AlbumArtUtil.getCurrentBodyColor();
            if (bodyColor != 0) {
                return bodyColor;
            }
        }

        return ThemeUtil.getBodyColor(getContext());
    }

    @Override
    public void onAlbumArtReady(Bitmap bitmap) {
        notifyPropertyChanged(BR.albumArt);
        notifyPropertyChanged(BR.backgroundColor);
        notifyPropertyChanged(BR.bodyColor);
    }

    @Bindable
    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
        notifyPropertyChanged(BR.isPlaying);
    }

    @Bindable
    public boolean getIsFavorited() {
        if (currentSong == null) {
            return false;
        }

        return currentSong.isFavorite();
    }

    public void setIsFavorited(boolean isFavorited) {
        if (currentSong == null) {
            return;
        }

        this.currentSong.setFavorite(isFavorited);
        notifyPropertyChanged(BR.isFavorited);
    }

    @Bindable
    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
        notifyPropertyChanged(BR.listeners);
    }

    @Bindable
    public String getRequester() {
        final Context context = getContext();
        if (context == null || requester == null || TextUtils.isEmpty(requester.getDisplayName())) {
            return null;
        }

        return String.format(context.getString(R.string.requested_by), requester.getDisplayName());
    }

    public void setRequester(User requester) {
        this.requester = requester;
        notifyPropertyChanged(BR.requester);
    }

    @Bindable
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
        notifyPropertyChanged(BR.event);
    }


    // History
    // ========================================================================

    public List<Song> getHistory() {
        List<Song> songs = new ArrayList<>();
        songs.add(currentSong);
        songs.add(lastSong);
        songs.add(secondLastSong);
        return songs;
    }

    @Bindable
    public Song getLastSong() {
        return lastSong;
    }

    public void setLastSong(Song lastSong) {
        this.lastSong = lastSong;
        notifyPropertyChanged(BR.lastSong);
    }

    @Bindable
    public Song getSecondLastSong() {
        return secondLastSong;
    }

    public void setSecondLastSong(Song secondLastSong) {
        this.secondLastSong = secondLastSong;
        notifyPropertyChanged(BR.secondLastSong);
    }


    // Queue
    // ========================================================================

    @Bindable
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        notifyPropertyChanged(BR.queueSize);
    }

    @Bindable
    public int getInQueueByUser() {
        return inQueueByUser;
    }

    public void setInQueueByUser(int inQueueByUser) {
        this.inQueueByUser = inQueueByUser;
        notifyPropertyChanged(BR.inQueueByUser);
    }

    @Bindable
    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
        notifyPropertyChanged(BR.queuePosition);
    }


    // Mini player
    // ========================================================================

    @Bindable
    public float getMiniPlayerAlpha() {
        return miniPlayerAlpha;
    }

    public void setMiniPlayerAlpha(float miniPlayerAlpha) {
        this.miniPlayerAlpha = miniPlayerAlpha;
        notifyPropertyChanged(BR.miniPlayerAlpha);
    }


    // Misc.
    // ========================================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.PREF_COLOR_NOW_PLAYING:
                notifyPropertyChanged(BR.backgroundColor);
                notifyPropertyChanged(BR.bodyColor);
                break;
        }
    }

}
