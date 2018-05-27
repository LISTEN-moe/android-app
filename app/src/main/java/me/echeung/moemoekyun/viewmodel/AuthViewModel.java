package me.echeung.moemoekyun.viewmodel;

import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

public class AuthViewModel extends BaseViewModel {

    private boolean showRegister = false;

    @Bindable
    public boolean getShowRegister() {
        return showRegister;
    }

    public void setShowRegister(boolean showRegister) {
        this.showRegister = showRegister;
        notifyPropertyChanged(BR.showRegister);
    }

}
