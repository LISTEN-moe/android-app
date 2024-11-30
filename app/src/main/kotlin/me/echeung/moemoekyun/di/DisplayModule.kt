package me.echeung.moemoekyun.di

import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.math.max

@Module
@InstallIn(SingletonComponent::class)
object DisplayModule {

    @Provides
    @Singleton
    @MaxBitmapSize
    fun maxBitmapSize(): Int {
        val displayMetrics = Resources.getSystem().displayMetrics
        return max(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    @Retention
    annotation class MaxBitmapSize
}
