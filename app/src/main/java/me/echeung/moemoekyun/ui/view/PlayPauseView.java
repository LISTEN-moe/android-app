package me.echeung.moemoekyun.ui.view;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import me.echeung.moemoekyun.R;

public class PlayPauseView {

    private final ImageView view;
    private final Drawable playDrawable;
    private final Drawable pauseDrawable;

    public PlayPauseView(Context context, ImageView view) {
        this.view = view;

        // AnimatedVectorDrawables don't work well below API 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            playDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_pause_to_play);
            pauseDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_play_to_pause);
        } else {
            playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_white_24dp);
            pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause_white_24dp);
        }
    }

    public void toggle(boolean isPlaying) {
        Drawable drawable = isPlaying ? pauseDrawable : playDrawable;
        view.setImageDrawable(drawable);

        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

}
