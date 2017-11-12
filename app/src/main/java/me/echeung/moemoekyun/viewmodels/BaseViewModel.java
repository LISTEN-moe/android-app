package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.utils.LocaleUtil;

public abstract class BaseViewModel extends BaseObservable {

    private WeakReference<Context> contextRef;

    public BaseViewModel(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    protected Context getContext() {
        final Context context = contextRef.get();
        if (context == null) {
            return null;
        }

        return LocaleUtil.setLocale(context);
    }

    // Network connection
    // ========================================================================

    private boolean isConnected;

    @Bindable
    public boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
        notifyPropertyChanged(BR.isConnected);
    }


    // Helpers
    // ========================================================================

    @BindingAdapter("android:visibility")
    public static void setVisibility(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("app:textStyle")
    public static void setTextStyle(TextView v, int typeface) {
        v.setTypeface(null, typeface);
    }
}
