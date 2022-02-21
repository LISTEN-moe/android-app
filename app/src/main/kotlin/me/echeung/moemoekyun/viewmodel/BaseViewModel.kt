package me.echeung.moemoekyun.viewmodel

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import me.echeung.moemoekyun.BR
import me.echeung.moemoekyun.util.ext.loadImage
import me.echeung.moemoekyun.util.ext.transitionBackgroundColor

abstract class BaseViewModel : BaseObservable() {

    // Network connection
    // ========================================================================

    @get:Bindable
    var isConnected: Boolean = false
        set(isConnected) {
            field = isConnected
            notifyPropertyChanged(BR.connected)
        }

    // Auth status
    // ========================================================================

    @get:Bindable
    var isAuthed: Boolean = false
        set(isAuthed) {
            field = isAuthed
            notifyPropertyChanged(BR.authed)
        }

    companion object {
        @JvmStatic
        @BindingAdapter("android:alpha")
        fun setAlpha(v: View, visible: Boolean) {
            v.alpha = (if (visible) 1 else 0).toFloat()
        }

        @JvmStatic
        @BindingAdapter("android:selected")
        fun setSelected(v: TextView, selected: Boolean) {
            v.isSelected = selected
        }

        @JvmStatic
        @BindingAdapter("android:visibility")
        fun setVisibility(v: View, visible: Boolean) {
            v.isVisible = visible
        }

        @JvmStatic
        @BindingAdapter("android:imageBitmap")
        fun loadImage(v: ImageView, bitmap: Bitmap?) {
            v.loadImage(bitmap)
        }

        @JvmStatic
        @BindingAdapter("android:imageUrl")
        fun loadImage(v: ImageView, url: String?) {
            v.loadImage(url)
        }

        @JvmStatic
        @BindingAdapter("android:transitionBackgroundColor")
        fun transitionBackgroundColor(v: View, @ColorInt toColor: Int) {
            v.transitionBackgroundColor(toColor)
        }
    }
}
