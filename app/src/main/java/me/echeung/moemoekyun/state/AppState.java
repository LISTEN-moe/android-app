package me.echeung.moemoekyun.state;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import me.echeung.moemoekyun.model.Song;

public class AppState extends BaseObservable {

    private static final AppState INSTANCE = new AppState();

    // Network state
    public final ObservableBoolean hasNetworkConnection = new ObservableBoolean();

    // Play state
    public final ObservableBoolean playing = new ObservableBoolean();
    public final ObservableField<Song> currentSong = new ObservableField<>();
    public final ObservableBoolean currentFavorited = new ObservableBoolean();
    public final ObservableInt listeners = new ObservableInt();
    public final ObservableField<String> requester = new ObservableField<>();

    private AppState() {}

    public static AppState getInstance() {
        return INSTANCE;
    }

    public void setFavorited(boolean favorited) {
        INSTANCE.currentSong.get().setFavorite(favorited);
        INSTANCE.currentFavorited.set(favorited);
    }
}