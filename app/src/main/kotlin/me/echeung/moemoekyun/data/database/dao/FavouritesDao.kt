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
        """,
    )
    abstract suspend fun getFavouriteSongs(): List<FavouriteSongView>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(favourite: FavouriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(favourites: List<FavouriteEntity>)

    @Query("DELETE FROM favourites WHERE songId = :songId")
    abstract suspend fun delete(songId: Int)

    @Query("DELETE FROM favourites")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(favourites: List<FavouriteEntity>) {
        deleteAll()
        insertAll(favourites)
    }
}
