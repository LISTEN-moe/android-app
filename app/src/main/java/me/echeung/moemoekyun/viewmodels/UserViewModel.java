package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import me.echeung.listenmoeapi.models.User;

public class UserViewModel extends BaseViewModel {

    private User user;
    private String avatarUrl;
    private String bannerUrl;

    private boolean hasFavorites;

    public UserViewModel(Context context) {
        super(context);
    }

    public void reset() {
        setUser(null);
        setAvatarUrl(null);
        setBannerUrl(null);

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
    public boolean getHasFavorites() {
        return hasFavorites;
    }

    public void setHasFavorites(boolean hasFavorites) {
        this.hasFavorites = hasFavorites;
        notifyPropertyChanged(BR.hasFavorites);
    }
}
