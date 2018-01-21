package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public final class AlbumArtUtil {

    public static void getAlbumArtBitmap(Context context, String url, Callback callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            callback.onBitmapReady(resource);
                        }
                    });
        });
    }

    public interface Callback {
        void onBitmapReady(Bitmap bitmap);
    }

}
