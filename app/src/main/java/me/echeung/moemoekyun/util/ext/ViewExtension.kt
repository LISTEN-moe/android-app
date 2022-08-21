package me.echeung.moemoekyun.util.ext

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import me.echeung.moemoekyun.R

private const val TRANSITION_DURATION = 250

fun ImageView.loadImage(bitmap: Bitmap?) {
    this.clear()

    if (bitmap == null) {
        return
    }

    Glide.with(context)
        .load(bitmap)
        .transition(DrawableTransitionOptions.withCrossFade(TRANSITION_DURATION))
        .apply(
            RequestOptions()
                .placeholder(drawable)
                .override(width, height)
                .centerCrop()
                .dontAnimate(),
        )
        .into(this)
}

fun ImageView.loadImage(url: String?) {
    this.clear()

    if (url == null) {
        return
    }

    Glide.with(context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade(TRANSITION_DURATION))
        .apply(
            RequestOptions()
                .placeholder(drawable)
                .override(width, height)
                .centerCrop()
                .dontAnimate(),
        )
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

/**
 * Shows a popup menu on top of this view.
 *
 * @param menuRes menu items to inflate the menu with.
 * @param initMenu function to execute when the menu after is inflated.
 * @param onMenuItemClick function to execute when a menu item is clicked.
 */
fun View.popupMenu(@MenuRes menuRes: Int, initMenu: (Menu.() -> Unit)? = null, onMenuItemClick: MenuItem.() -> Boolean) {
    val popup = PopupMenu(context, this, Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0)
    popup.menuInflater.inflate(menuRes, popup.menu)

    if (initMenu != null) {
        popup.menu.initMenu()
    }
    popup.setOnMenuItemClickListener { it.onMenuItemClick() }

    popup.show()
}

private fun ImageView?.clear() {
    if (this == null) return

    Glide.with(context).clear(this)
}
