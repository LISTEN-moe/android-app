package me.echeung.moemoekyun.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import me.echeung.moemoekyun.domain.songs.model.DomainSong

@Entity(
    tableName = "favourites",
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
    @PrimaryKey val songId: Int,
    val favoritedAtEpoch: Long?,
)

fun DomainSong.toFavouriteEntity() = FavouriteEntity(
    songId = id,
    favoritedAtEpoch = favoritedAtEpoch,
)
