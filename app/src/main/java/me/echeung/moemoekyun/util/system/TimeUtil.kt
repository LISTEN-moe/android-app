package me.echeung.moemoekyun.util.system

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// Based on https://stackoverflow.com/a/10621553
object TimeUtil {

    // Example string: "2018-01-20T10:36:35.680Z"
    @Throws(ParseException::class)
    fun toCalendar(iso8601string: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = simpleDateFormat.parse(iso8601string)
        calendar.time = date
        return calendar
    }
}
