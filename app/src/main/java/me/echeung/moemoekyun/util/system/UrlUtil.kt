package me.echeung.moemoekyun.util.system

import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlUtil {

    @JvmStatic
    fun open(context: Context, url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }

}
