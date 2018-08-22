package me.echeung.moemoekyun.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.util.ImageUtil;
import me.echeung.moemoekyun.util.ViewUtil;

public abstract class BaseViewModel extends BaseObservable {

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


    // Auth status
    // ========================================================================

    private boolean isAuthed;

    @Bindable
    public boolean getIsAuthed() {
        return isAuthed;
    }

    public void setIsAuthed(boolean isAuthed) {
        this.isAuthed = isAuthed;
        notifyPropertyChanged(BR.isAuthed);
    }


    // Helpers
    // ========================================================================

    @BindingAdapter("android:alpha")
    public static void setAlpha(View v, boolean visible) {
        v.setAlpha(visible ? 1 : 0);
    }

    @BindingAdapter("android:selected")
    public static void setSelected(TextView v, boolean selected) {
        v.setSelected(selected);
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("android:imageBitmap")
    public static void loadImage(ImageView v, Bitmap bitmap) {
        ImageUtil.loadImage(v, bitmap);
    }

    @BindingAdapter("android:imageUrl")
    public static void loadImage(ImageView v, String url) {
        ImageUtil.loadImage(v, url);
    }

    @BindingAdapter("android:transitionBackgroundColor")
    public static void transitionBackgroundColor(View v, @ColorInt int toColor) {
        ViewUtil.transitionBackgroundColor(v, toColor);
    }

}
