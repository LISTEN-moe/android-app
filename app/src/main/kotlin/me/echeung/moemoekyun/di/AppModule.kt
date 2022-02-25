package me.echeung.moemoekyun.di

import kotlinx.serialization.json.Json
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongFormatter
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.system.LocaleUtil
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AlbumArtUtil(androidContext()) }
    single { AuthUtil(androidContext()) }
    single { LocaleUtil(get()) }
    single { NetworkClient(androidContext(), get()) }
    single { PreferenceUtil(androidContext()) }
    single { SongActionsUtil(get(), get(), get(), get()) }
    single { SongFormatter(get()) }
    single { SongSortUtil(get()) }
    single { Json { ignoreUnknownKeys = true } }
}

val radioModule = module {
    single { Stream(androidContext()) }
    single { Socket(androidContext(), get(), get()) }
    single { RadioClient(get(), get(), get(), get(), get()) }
}

val viewModelModule = module {
    single { UserViewModel() }
    single { RadioViewModel(get(), get()) }
}
