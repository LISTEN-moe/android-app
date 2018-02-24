package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.models.User;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.AlbumArtUtil;
import me.echeung.moemoekyun.utils.PluralsUtil;

public class RadioViewModel extends BaseViewModel implements AlbumArtUtil.Callback {

    private Song currentSong;

    private boolean isPlaying;
    private int listeners;
    private User requester;
    private String event;

    private Song lastSong;
    private Song secondLastSong;

    private int queueSize;
    private int inQueueByUser;
    private int queuePosition;

    private float miniPlayerAlpha;

    public RadioViewModel(Context context) {
        super(context);

        AlbumArtUtil.addListener(this);

        // Defaults
        isPlaying = false;
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
    public String getListeners() {
        final Context context = getContext();
        if (context == null) {
            return "";
        }

        try {
            return PluralsUtil.getString(context, R.plurals.current_listeners, listeners);
        } catch (NumberFormatException e) {
        }

        return "";
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
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
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

}
