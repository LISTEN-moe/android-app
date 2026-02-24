package me.echeung.moemoekyun.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import me.echeung.moemoekyun.data.database.FavouriteSongView
import me.echeung.moemoekyun.data.database.entity.FavouriteEntity

@Dao
abstract class FavouritesDao {

    @Query(
        """
        SELECT songs.*, favourites.favoritedAtEpoch
        FROM songs
        INNER JOIN favourites ON songs.id = favourites.songId
        WHERE favourites.station = :station
        """,
    )
    abstract suspend fun getFavouriteSongs(station: String): List<FavouriteSongView>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(favourite: FavouriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(favourites: List<FavouriteEntity>)

    @Query("DELETE FROM favourites WHERE songId = :songId AND station = :station")
    abstract suspend fun delete(songId: Int, station: String)

    @Query("DELETE FROM favourites WHERE station = :station")
    abstract suspend fun deleteAllForStation(station: String)

    @Query("DELETE FROM favourites")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(favourites: List<FavouriteEntity>, station: String) {
        deleteAllForStation(station)
        insertAll(favourites)
    }
}
