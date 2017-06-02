package jcotter.listenmoe.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

// Based on https://stackoverflow.com/a/9288544
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView mImage;

    public DownloadImageTask(ImageView bmImage) {
        this.mImage = bmImage;
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
        mImage.setImageBitmap(result);
    }
}
