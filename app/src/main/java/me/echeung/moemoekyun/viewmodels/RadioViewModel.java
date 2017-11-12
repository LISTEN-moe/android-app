package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.Bindable;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.PluralsUtil;

public class RadioViewModel extends BaseViewModel {

    private static final String SHOW_HISTORY = "pref_show_history";

    private final SharedPreferences sharedPrefs;

    private Song currentSong;
    private boolean isPlaying;
    private boolean isFavorited;
    private String listeners;
    private String requester;

    private boolean showHistory;
    private String lastSong;
    private String secondLastSong;

    public RadioViewModel(Context context) {
        super(context);

        // Defaults
        isPlaying = false;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        showHistory = sharedPrefs.getBoolean(SHOW_HISTORY, false);
    }

    public void reset() {
        setCurrentSong(null);
        setLastSong(null);
        setSecondLastSong(null);

        setListeners(null);
        setRequester(null);
    }


    // Play state
    // ========================================================================

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
    public String getListeners() {
        final Context context = getContext();
        if (context == null || listeners == null) {
            return "";
        }

        try {
            Integer val = Integer.valueOf(listeners);
            if (val != null) {
                return PluralsUtil.getString(context, R.plurals.current_listeners, val);
            }
        } catch (NumberFormatException e) {
        }

        return listeners;
    }

    public void setListeners(String listeners) {
        this.listeners = listeners;
        notifyPropertyChanged(BR.listeners);
    }

    @Bindable
    public String getRequester() {
        final Context context = getContext();
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

    @Bindable
    public boolean getShowHistory() {
        return showHistory;
    }

    public void toggleShowHistory() {
        showHistory = !showHistory;
        notifyPropertyChanged(BR.showHistory);

        sharedPrefs.edit()
                .putBoolean(SHOW_HISTORY, showHistory)
                .apply();
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
}
