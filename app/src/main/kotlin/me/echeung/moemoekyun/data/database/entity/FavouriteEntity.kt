package me.echeung.moemoekyun.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.songs.model.DomainSong

@Entity(
    tableName = "favourites",
    primaryKeys = ["songId", "station"],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("songId")],
)
data class FavouriteEntity(
    val songId: Int,
    val station: String,
    val favoritedAtEpoch: Long?,
)

fun DomainSong.toFavouriteEntity(station: Station) = FavouriteEntity(
    songId = id,
    station = station.name,
    favoritedAtEpoch = favoritedAtEpoch,
)
