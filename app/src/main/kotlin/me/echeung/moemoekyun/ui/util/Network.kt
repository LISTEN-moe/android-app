package me.echeung.moemoekyun.ui.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import me.echeung.moemoekyun.util.ext.connectivityManager
import me.echeung.moemoekyun.util.ext.isWifiConnected

/**
 * Remembers and observes the Wi-Fi connection status.
 * Returns a State that automatically updates when Wi-Fi connectivity changes.
 */
@Composable
fun rememberIsWifiConnected(): State<Boolean> {
    val context = LocalContext.current
    val wifiState = remember { mutableStateOf(context.isWifiConnected()) }
    val wifiNetworks = remember { mutableSetOf<Network>() }

    DisposableEffect(context) {
        val connectivityManager = context.connectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                wifiNetworks.add(network)
                wifiState.value = true
            }

            override fun onLost(network: Network) {
                wifiNetworks.remove(network)
                wifiState.value = wifiNetworks.isNotEmpty()
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    return wifiState
}
