package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

public final class AlbumArtUtil {

    public static void getAlbumArtBitmap(Context context, String url, Callback callback) {
        getAlbumArtBitmap(context, url, Target.SIZE_ORIGINAL, callback);
    }

    public static void getAlbumArtBitmap(Context context, String url, int size, Callback callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .into(new SimpleTarget<Bitmap>(size, size) {
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
