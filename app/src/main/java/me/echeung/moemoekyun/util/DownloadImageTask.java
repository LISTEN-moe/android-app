package me.echeung.moemoekyun.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

// Based on https://stackoverflow.com/a/9288544
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private WeakReference<ImageView> imageViewRef;

    public DownloadImageTask(ImageView imageView) {
        this.imageViewRef = new WeakReference<>(imageView);
    }

    protected Bitmap doInBackground(String... urls) {
        Bitmap icon = null;

        try {
            InputStream in = new java.net.URL(urls[0]).openStream();
            icon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return icon;
    }

    protected void onPostExecute(Bitmap result) {
        ImageView imageView = imageViewRef.get();
        if (imageView != null) {
            imageView.setImageBitmap(result);
        }
    }
}
