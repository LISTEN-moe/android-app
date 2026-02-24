package me.echeung.moemoekyun.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.echeung.moemoekyun.data.database.AppDatabase
import me.echeung.moemoekyun.data.database.dao.FavouritesDao
import me.echeung.moemoekyun.data.database.dao.SongsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun appDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "moemoekyun.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun songsDao(db: AppDatabase): SongsDao = db.songsDao()

    @Provides
    @Singleton
    fun favouritesDao(db: AppDatabase): FavouritesDao = db.favouritesDao()
}
