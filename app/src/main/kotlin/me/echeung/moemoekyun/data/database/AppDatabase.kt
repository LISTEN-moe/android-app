package me.echeung.moemoekyun.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.echeung.moemoekyun.data.database.dao.FavouritesDao
import me.echeung.moemoekyun.data.database.dao.SongsDao
import me.echeung.moemoekyun.data.database.entity.FavouriteEntity
import me.echeung.moemoekyun.data.database.entity.SongEntity

@Database(
    entities = [SongEntity::class, FavouriteEntity::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songsDao(): SongsDao

    abstract fun favouritesDao(): FavouritesDao
}
