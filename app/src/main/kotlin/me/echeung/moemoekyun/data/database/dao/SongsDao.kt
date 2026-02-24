package me.echeung.moemoekyun.data.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import me.echeung.moemoekyun.data.database.entity.SongEntity

@Dao
interface SongsDao {
    @Upsert
    suspend fun upsert(song: SongEntity)

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)
}
