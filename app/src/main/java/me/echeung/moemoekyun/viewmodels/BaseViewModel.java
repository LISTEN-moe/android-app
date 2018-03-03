package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.lang.ref.WeakReference;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.utils.LocaleUtil;
import me.echeung.moemoekyun.utils.ThemeUtil;

public abstract class BaseViewModel extends BaseObservable {

    private WeakReference<Context> contextRef;

    public BaseViewModel(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    protected Context getContext() {
        Context context = contextRef.get();
        if (context == null) {
            return null;
        }

        context = LocaleUtil.setLocale(context);
        context = ThemeUtil.setTheme(context);

        return context;
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
        // Free up previous resources
        Glide.with(v.getContext())
                .clear(v);

        if (App.getPreferenceUtil().shouldDownloadImage(v.getContext())) {
            Glide.with(v.getContext())
                    .load(bitmap)
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(v.getDrawable()))
                    .into(v);
        }
    }

    @BindingAdapter("android:imageUrl")
    public static void loadImage(ImageView v, String url) {
        // Free up previous resources
        Glide.with(v.getContext())
                .clear(v);

        if (App.getPreferenceUtil().shouldDownloadImage(v.getContext())) {
            Glide.with(v.getContext())
                    .load(url)
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(v.getDrawable()))
                    .into(new DrawableImageViewTarget(v).waitForLayout());
        }
    }

}
