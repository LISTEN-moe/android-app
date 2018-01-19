package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.Bindable;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.models.User;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.PluralsUtil;

public class RadioViewModel extends BaseViewModel {

    private static final String SHOW_HISTORY = "pref_show_history";

    private final SharedPreferences sharedPrefs;

    private Song currentSong;
    private boolean isPlaying;
    private boolean isFavorited;
    private int listeners;
    private User requester;
    private String event;

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

        setListeners(0);
        setRequester(null);
        setEvent(null);
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
        if (this.currentSong == null) {
            return;
        }

//        this.currentSong.setFavorite(isFavorited);
        this.isFavorited = isFavorited;
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
        if (context == null || requester == null || TextUtils.isEmpty(requester.getUsername())) {
            return null;
        }

        return String.format(context.getString(R.string.requested_by), requester.getUsername());
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

    public String getRequesterOrEvent() {
        if (event != null && !event.isEmpty()) {
            return event;
        }

        return getRequester();
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
