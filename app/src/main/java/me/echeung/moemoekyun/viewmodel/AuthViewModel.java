package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

public class AuthViewModel extends BaseViewModel {

    private boolean showRegister;

    public AuthViewModel(Context context) {
        super(context);

        // Defaults
        showRegister = false;
    }

    @Bindable
    public boolean getShowRegister() {
        return showRegister;
    }

    public void setShowRegister(boolean showRegister) {
        this.showRegister = showRegister;
        notifyPropertyChanged(BR.showRegister);
    }

}
