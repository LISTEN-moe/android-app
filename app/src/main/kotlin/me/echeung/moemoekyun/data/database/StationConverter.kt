package me.echeung.moemoekyun.data.database

import androidx.room.TypeConverter
import me.echeung.moemoekyun.client.api.Station

class StationConverter {
    @TypeConverter
    fun fromStation(station: Station): String = station.name

    @TypeConverter
    fun toStation(name: String): Station = Station.valueOf(name)
}
