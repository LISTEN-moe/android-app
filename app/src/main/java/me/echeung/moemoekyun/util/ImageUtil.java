package me.echeung.moemoekyun.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import me.echeung.moemoekyun.App;

public class ImageUtil {

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
                    .into(v);
        }
    }

}
