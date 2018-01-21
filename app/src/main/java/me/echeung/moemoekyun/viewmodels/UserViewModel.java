package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import me.echeung.listenmoeapi.models.User;

public class UserViewModel extends BaseViewModel {

    private User user;
    private String avatarUrl;
    private String bannerUrl;

    private int queueSize;
    private int queuePosition;
    private boolean hasFavorites;

    public UserViewModel(Context context) {
        super(context);
    }

    public void reset() {
        setUser(null);
        setAvatarUrl(null);
        setBannerUrl(null);

        setQueueSize(0);
        setQueuePosition(0);
        setHasFavorites(false);
    }

    @Bindable
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        notifyPropertyChanged(BR.user);
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
        notifyPropertyChanged(BR.bannerUrl);
    }

    @Bindable
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        notifyPropertyChanged(BR.queueSize);
    }

    @Bindable
    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
        notifyPropertyChanged(BR.queuePosition);
    }

    @Bindable
    public boolean getHasFavorites() {
        return hasFavorites;
    }

    public void setHasFavorites(boolean hasFavorites) {
        this.hasFavorites = hasFavorites;
        notifyPropertyChanged(BR.hasFavorites);
    }
}
