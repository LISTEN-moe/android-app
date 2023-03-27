package me.echeung.moemoekyun.util.system

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// Based on https://stackoverflow.com/a/10621553
object TimeUtil {

    // Example string: "2018-01-20T10:36:35.680Z"
    @Throws(ParseException::class)
    fun String.toCalendar(): Calendar {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return Calendar.getInstance().apply {
            time = simpleDateFormat.parse(this@toCalendar)!!
        }
    }
}
