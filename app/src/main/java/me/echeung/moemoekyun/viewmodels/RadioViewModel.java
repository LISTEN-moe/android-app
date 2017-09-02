package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.text.TextUtils;

import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.api.v3.model.Song;
import me.echeung.moemoekyun.viewmodels.base.BaseViewModel;

public class RadioViewModel extends BaseViewModel {

    public RadioViewModel(Context context) {
        super(context);
    }


    // Play state
    // ========================================================================

    private Song currentSong;
    private boolean isPlaying = false;
    private boolean isFavorited;
    private int listeners;
    private String requester;

    @Bindable
    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
        notifyPropertyChanged(BR.currentSong);
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
        return isFavorited;
    }

    public void setIsFavorited(boolean isFavorited) {
        this.currentSong.setFavorite(isFavorited);
        this.isFavorited = isFavorited;
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
        final Context context = contextRef.get();
        if (context == null || TextUtils.isEmpty(requester)) {
            return null;
        }

        // If there's a space, it's probably an event
        if (requester.contains(" ")) {
            return requester;
        }

        // Actual user requester
        return String.format(context.getString(R.string.requested_by), requester);
    }

    public void setRequester(String requester) {
        this.requester = requester;
        notifyPropertyChanged(BR.requester);
    }


    // History
    // ========================================================================

    private boolean showHistory;
    private String lastSong;
    private String secondLastSong;

    @Bindable
    public boolean getShowHistory() {
        return showHistory;
    }

    public void toggleShowHistory() {
        showHistory = !showHistory;
        notifyPropertyChanged(BR.showHistory);
    }

    @Bindable
    public String getLastSong() {
        return TextUtils.isEmpty(lastSong) ? null : lastSong;
    }

    public void setLastSong(String lastSong) {
        this.lastSong = lastSong;
        notifyPropertyChanged(BR.lastSong);
    }

    @Bindable
    public String getSecondLastSong() {
        return TextUtils.isEmpty(secondLastSong) ? null : secondLastSong;
    }

    public void setSecondLastSong(String secondLastSong) {
        this.secondLastSong = secondLastSong;
        notifyPropertyChanged(BR.secondLastSong);
    }


    // Helpers
    // ========================================================================
    
    public void clear() {
        setCurrentSong(null);
        setLastSong(null);
        setSecondLastSong(null);

        setListeners(0);
        setRequester(null);
    }
}