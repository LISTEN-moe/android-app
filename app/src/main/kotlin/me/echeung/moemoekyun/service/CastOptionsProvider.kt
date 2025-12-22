package me.echeung.moemoekyun.service

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

// https://cast.google.com/publish/#/applications/edit/6E4C737C
private const val CAST_RECEIVER_ID: String = "6E4C737C"

@UnstableApi
class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions = CastOptions.Builder()
        .setResumeSavedSession(false)
        .setEnableReconnectionService(false)
        .setReceiverApplicationId(CAST_RECEIVER_ID)
        .setStopReceiverApplicationWhenEndingSession(true)
        .setRemoteToLocalEnabled(true)
        .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider?> = emptyList()
}
