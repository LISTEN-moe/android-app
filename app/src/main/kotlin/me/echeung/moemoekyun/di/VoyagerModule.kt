package me.echeung.moemoekyun.di

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.hilt.ScreenModelFactory
import cafe.adriel.voyager.hilt.ScreenModelFactoryKey
import cafe.adriel.voyager.hilt.ScreenModelKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import me.echeung.moemoekyun.ui.screen.auth.LoginScreenModel
import me.echeung.moemoekyun.ui.screen.auth.RegisterScreenModel
import me.echeung.moemoekyun.ui.screen.home.HomeScreenModel
import me.echeung.moemoekyun.ui.screen.search.SearchScreenModel
import me.echeung.moemoekyun.ui.screen.settings.SettingsScreenModel
import me.echeung.moemoekyun.ui.screen.songs.SongsScreenModel

@Module
@InstallIn(ActivityComponent::class)
abstract class VoyagerModule {

    @Binds
    @IntoMap
    @ScreenModelKey(HomeScreenModel::class)
    abstract fun homeScreenModel(homeScreenModel: HomeScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(LoginScreenModel::class)
    abstract fun loginScreenModel(loginScreenModel: LoginScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(RegisterScreenModel::class)
    abstract fun registerScreenModel(registerScreenModel: RegisterScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(SearchScreenModel::class)
    abstract fun searchScreenModel(searchScreenModel: SearchScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(SettingsScreenModel::class)
    abstract fun settingsScreenModel(settingsScreenModel: SettingsScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelFactoryKey(SongsScreenModel.Factory::class)
    abstract fun songsScreenModelFactory(songsScreenModelFactory: SongsScreenModel.Factory): ScreenModelFactory
}
