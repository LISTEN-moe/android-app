package me.echeung.moemoekyun.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.databinding.Bindable;
import android.graphics.Bitmap;
import androidx.annotation.ColorInt;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.client.model.Event;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.client.model.User;
import me.echeung.moemoekyun.util.AlbumArtUtil;
import me.echeung.moemoekyun.util.PreferenceUtil;
import me.echeung.moemoekyun.util.system.ThemeUtil;

public class RadioViewModel extends BaseViewModel implements AlbumArtUtil.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    private Song currentSong;

    private boolean isPlaying = false;
    private int listeners;
    private User requester;
    private Event event;

    private Song lastSong;
    private Song secondLastSong;

    private int queueSize;
    private int inQueueByUser;
    private int queuePosition;

    private float miniPlayerAlpha;

    public RadioViewModel() {
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

        AlbumArtUtil.updateAlbumArt(App.getContext(), currentSong);

        notifyPropertyChanged(BR.currentSong);
    }

    @Bindable
    public Bitmap getAlbumArt() {
        return AlbumArtUtil.getCurrentAlbumArt();
    }

    // Indirectly bind to albumArt: https://stackoverflow.com/a/39087434
    @ColorInt
    public int getBackgroundColor(Context context, Bitmap albumArt) {
        if (App.getPreferenceUtil().shouldColorNowPlaying() && !AlbumArtUtil.isDefaultAlbumArt()) {
            int accentColor = AlbumArtUtil.getCurrentAccentColor();
            if (accentColor != 0) {
                return accentColor;
            }
        }

        return ThemeUtil.getBackgroundColor(context);
    }

    // Indirectly bind to albumArt: https://stackoverflow.com/a/39087434
    @ColorInt
    public int getBodyColor(Context context, Bitmap albumArt) {
        if (App.getPreferenceUtil().shouldColorNowPlaying() && !AlbumArtUtil.isDefaultAlbumArt()) {
            int bodyColor = AlbumArtUtil.getCurrentBodyColor();
            if (bodyColor != 0) {
                return bodyColor;
            }
        }

        return ThemeUtil.getBodyColor(context);
    }

    @Override
    public void onAlbumArtReady(Bitmap bitmap) {
        notifyPropertyChanged(BR.albumArt);
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
        if (requester == null || TextUtils.isEmpty(requester.getDisplayName())) {
            return null;
        }

        return requester.getDisplayName();
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
            case PreferenceUtil.PREF_GENERAL_ROMAJI:
                notifyPropertyChanged(BR.currentSong);
                break;

            case PreferenceUtil.PREF_COLOR_NOW_PLAYING:
                notifyPropertyChanged(BR.albumArt);
                break;
        }
    }

}
