package me.echeung.moemoekyun.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import me.echeung.moemoekyun.R

class PlayPauseView(private val context: Context, private val view: ImageView) {

    private val playDrawable: Drawable
    private val pauseDrawable: Drawable

    init {
        // AnimatedVectorDrawables don't work well below API 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            playDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_pause_to_play)!!
            pauseDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_play_to_pause)!!
        } else {
            playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_24dp)!!
            pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause_24dp)!!
        }
    }

    fun toggle(isPlaying: Boolean, isLoading: Boolean) {
        val drawable = when {
            isLoading -> CircularProgressDrawable(context).apply {
                setColorSchemeColors(Color.WHITE)
                start()
            }
            isPlaying -> pauseDrawable
            else -> playDrawable
        }
        view.setImageDrawable(drawable)
        (drawable as? Animatable)?.start()
    }
}
