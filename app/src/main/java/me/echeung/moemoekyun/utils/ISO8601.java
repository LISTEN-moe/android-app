package me.echeung.moemoekyun.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

// Based on https://stackoverflow.com/a/10621553
public final class ISO8601 {

    // Example string: "2018-01-20T10:36:35.680Z"
    public static Calendar toCalendar(String iso8601string) throws ParseException {
        Calendar calendar = GregorianCalendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(iso8601string);
        calendar.setTime(date);
        return calendar;
    }

}
