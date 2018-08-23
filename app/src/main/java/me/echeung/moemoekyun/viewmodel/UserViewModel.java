package me.echeung.moemoekyun.viewmodel;

import androidx.databinding.Bindable;

import androidx.databinding.library.baseAdapters.BR;

import me.echeung.moemoekyun.client.model.User;

public class UserViewModel extends BaseViewModel {

    private User user;
    private String avatarUrl;
    private String bannerUrl;

    private boolean hasFavorites;

    public void reset() {
        setUser(null);
        setAvatarUrl(null);
        setBannerUrl(null);

        setRequestsRemaining(0);
        setHasFavorites(false);
    }

    @Bindable
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        setRequestsRemaining(user != null ? user.getRequestsRemaining() : 0);
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
    public int getRequestsRemaining() {
        if (user == null) {
            return 0;
        }

        return user.getRequestsRemaining();
    }

    public void setRequestsRemaining(int requestsRemaining) {
        if (user != null) {
            user.setRequestsRemaining(requestsRemaining);
        }
        notifyPropertyChanged(BR.requestsRemaining);
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
