package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;

public final class AlbumArtUtil {

    private static Bitmap defaultAlbumArt;

    public static void getAlbumArtBitmap(Context context, Song song, Callback callback) {
        getAlbumArtBitmap(context, song, Target.SIZE_ORIGINAL, callback);
    }

    public static void getAlbumArtBitmap(Context context, Song song, int size, Callback callback) {
        final String albumArtUrl = song.getAlbumArtUrl();
        if (albumArtUrl != null) {
            downloadAlbumArtBitmap(context, albumArtUrl, size, callback);
            return;
        }

        callback.onBitmapReady(getDefaultAlbumArt(context));
    }

    private static void downloadAlbumArtBitmap(Context context, String url, int size, Callback callback) {
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

    private static Bitmap getDefaultAlbumArt(Context context) {
        if (defaultAlbumArt == null) {
            defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.album);
        }

        return defaultAlbumArt;
    }

    public interface Callback {
        void onBitmapReady(Bitmap bitmap);
    }

}
