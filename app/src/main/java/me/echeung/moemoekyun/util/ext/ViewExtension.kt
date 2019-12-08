package me.echeung.moemoekyun.util.ext

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import me.echeung.moemoekyun.App

private const val TRANSITION_DURATION = 250

fun ImageView.loadImage(bitmap: Bitmap?) {
    this.clear()

    if (!App.preferenceUtil!!.shouldDownloadImage(context) || bitmap == null) {
        return
    }

    Glide.with(context)
            .load(bitmap)
            .transition(DrawableTransitionOptions.withCrossFade(TRANSITION_DURATION))
            .apply(RequestOptions()
                    .placeholder(drawable)
                    .override(width, height)
                    .centerCrop()
                    .dontAnimate())
            .into(this)
}

fun ImageView.loadImage(url: String?) {
    this.clear()

    if (!App.preferenceUtil!!.shouldDownloadImage(context) || url == null) {
        return
    }

    Glide.with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade(TRANSITION_DURATION))
            .apply(RequestOptions()
                    .placeholder(drawable)
                    .override(width, height)
                    .centerCrop()
                    .dontAnimate())
            .into(this)
}

fun EditText.getTrimmedText(): String {
    return this.text.toString().trim()
}

fun View.transitionBackgroundColor(@ColorInt toColor: Int) {
    if (background == null) {
        setBackgroundColor(toColor)
        return
    }

    val fromColor = (background as ColorDrawable).color

    val valueAnimator = ValueAnimator.ofArgb(fromColor, toColor)
    valueAnimator.duration = TRANSITION_DURATION.toLong()
    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.addUpdateListener { animator -> setBackgroundColor(animator.animatedValue as Int) }
    valueAnimator.start()
}

fun View.toggleVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

private fun ImageView?.clear() {
    if (this == null) return

    Glide.with(context).clear(this)
}
