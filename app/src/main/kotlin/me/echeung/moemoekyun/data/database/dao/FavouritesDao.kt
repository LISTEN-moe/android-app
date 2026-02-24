package me.echeung.moemoekyun.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import me.echeung.moemoekyun.data.database.FavouriteSongView
import me.echeung.moemoekyun.data.database.entity.FavouriteEntity

@Dao
interface FavouritesDao {

    @Transaction
    @Query(
        """
        SELECT songs.*, favourites.favoritedAtEpoch
        FROM songs
        INNER JOIN favourites ON songs.id = favourites.songId
        """,
    )
    suspend fun getFavouriteSongs(): List<FavouriteSongView>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favourite: FavouriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favourites: List<FavouriteEntity>)

    @Query("DELETE FROM favourites WHERE songId = :songId")
    suspend fun delete(songId: Int)

    @Query("DELETE FROM favourites")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(favourites: List<FavouriteEntity>) {
        deleteAll()
        insertAll(favourites)
    }
}
