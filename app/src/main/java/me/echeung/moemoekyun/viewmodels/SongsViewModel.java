package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.text.TextUtils;

import com.android.databinding.library.baseAdapters.BR;

import me.echeung.moemoekyun.R;

public class SongsViewModel extends BaseViewModel {

    private boolean loadedSongs;

    public SongsViewModel(Context context) {
        super(context);

        // Defaults
        loadedSongs = false;
    }

    public void reset() {
        setLoadedSongs(false);
    }

    @Bindable
    public boolean isLoadedSongs() {
        return loadedSongs;
    }

    public void setLoadedSongs(boolean loadedSongs) {
        this.loadedSongs = loadedSongs;
        notifyPropertyChanged(BR.loadedSongs);
    }

}
