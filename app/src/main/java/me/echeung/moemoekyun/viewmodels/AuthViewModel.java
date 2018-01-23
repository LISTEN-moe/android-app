package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

public class AuthViewModel extends BaseViewModel {

    private boolean showRegister;
    private boolean showMfa;

    public AuthViewModel(Context context) {
        super(context);

        // Defaults
        showRegister = false;
        showMfa = false;
    }

    public void reset() {
        setShowRegister(false);
        setShowMfa(false);
    }

    @Bindable
    public boolean getShowRegister() {
        return showRegister;
    }

    public void setShowRegister(boolean showRegister) {
        this.showRegister = showRegister;
        notifyPropertyChanged(BR.showRegister);
    }

    @Bindable
    public boolean getShowMfa() {
        return showMfa;
    }

    public void setShowMfa(boolean showMfa) {
        this.showMfa = showMfa;
        notifyPropertyChanged(BR.showMfa);
    }

}
