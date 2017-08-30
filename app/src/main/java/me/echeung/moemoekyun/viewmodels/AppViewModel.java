package me.echeung.moemoekyun.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.view.View;

import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.api.v3.model.Song;

public class AppViewModel extends BaseObservable {

    private static final AppViewModel INSTANCE = new AppViewModel();

    public static AppViewModel getInstance() {
        return INSTANCE;
    }

    private AppViewModel() {}


    // Network connection
    // ========================================================================

    private boolean isConnected;

    @Bindable
    public boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
        notifyPropertyChanged(BR.isConnected);
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    // Play state
    // ========================================================================

    private Song currentSong;
    private boolean isPlaying;
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
        return requester;
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
        return lastSong;
    }

    public void setLastSong(String lastSong) {
        this.lastSong = lastSong;
        notifyPropertyChanged(BR.lastSong);
    }

    @Bindable
    public String getSecondLastSong() {
        return secondLastSong;
    }

    public void setSecondLastSong(String secondLastSong) {
        this.secondLastSong = secondLastSong;
        notifyPropertyChanged(BR.secondLastSong);
    }
}