package me.echeung.moemoekyun.cast

import android.content.Context
import android.view.Menu
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.viewmodel.RadioViewModel

/**
 * No-op versions of functions used in Play Store version for Google Cast support
 */
class CastDelegate(
    context: Context,
    radioViewModel: RadioViewModel,
    stream: Stream,
    socket: Socket,
) {

    fun onDestroy() {
    }

    fun initCastButton(menu: Menu?) {
        menu?.findItem(R.id.media_route_menu_item)?.isVisible = false
    }
}
