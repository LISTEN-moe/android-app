package me.echeung.moemoekyun.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    fun scope(): CoroutineScope = MainScope()
}
