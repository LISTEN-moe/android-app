package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.Bindable;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.models.User;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.PluralsUtil;

public class RadioViewModel extends BaseViewModel {

    private static final String SHOW_HISTORY = "pref_show_history";

    private final SharedPreferences sharedPrefs;

    private Song currentSong;
    private String albumArtUrl;

    private boolean isPlaying;
    private int listeners;
    private User requester;
    private String event;

    private boolean showHistory;
    private Song lastSong;
    private Song secondLastSong;

    private int queueSize;
    private int inQueueByUser;
    private int queuePosition;

    public RadioViewModel(Context context) {
        super(context);

        // Defaults
        isPlaying = false;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        showHistory = sharedPrefs.getBoolean(SHOW_HISTORY, false);
    }

    public void reset() {
        setCurrentSong(null);
        setAlbumArtUrl(null);

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
        notifyPropertyChanged(BR.currentSong);
    }

    @Bindable
    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
        notifyPropertyChanged(BR.albumArtUrl);
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

}
