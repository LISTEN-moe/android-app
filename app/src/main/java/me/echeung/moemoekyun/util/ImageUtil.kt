package me.echeung.moemoekyun.util

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

import me.echeung.moemoekyun.App

object ImageUtil {

    @JvmStatic
    fun loadImage(v: ImageView, bitmap: Bitmap?) {
        clearImageView(v)

        if (!App.preferenceUtil!!.shouldDownloadImage(v.context) || bitmap == null) {
            return
        }

        Glide.with(v.context)
                .load(bitmap)
                .transition(DrawableTransitionOptions.withCrossFade(ViewUtil.TRANSITION_DURATION))
                .apply(RequestOptions()
                        .placeholder(v.drawable)
                        .override(v.width, v.height)
                        .centerCrop()
                        .dontAnimate())
                .into(v)
    }

    @JvmStatic
    fun loadImage(v: ImageView, url: String?) {
        clearImageView(v)

        if (!App.preferenceUtil!!.shouldDownloadImage(v.context) || url == null) {
            return
        }

        Glide.with(v.context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(ViewUtil.TRANSITION_DURATION))
                .apply(RequestOptions()
                        .placeholder(v.drawable)
                        .override(v.width, v.height)
                        .centerCrop()
                        .dontAnimate())
                .into(v)
    }

    @JvmStatic
    fun clearCache(context: Context?) {
        if (context == null) return

        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                Glide.get(context).clearDiskCache()
                return null
            }
        }.execute()

        Glide.get(context).clearMemory()
    }

    @JvmStatic
    private fun clearImageView(v: ImageView?) {
        if (v == null) return

        val context = v.context ?: return

        Glide.with(context).clear(v)
    }

}
