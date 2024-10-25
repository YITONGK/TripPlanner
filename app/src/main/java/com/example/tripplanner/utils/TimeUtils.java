package com.example.tripplanner.utils;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public final static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public final static SimpleDateFormat CALENDAR_DATE_FORMAT = new SimpleDateFormat("EEE MMM d, yyyy", Locale.getDefault());
    public static String convertTimestampToString(Timestamp timestamp){
        return DEFAULT_DATE_FORMAT.format(timestamp.toDate());
    }

    public static String convertTimestampToStringForCalendar(Timestamp timestamp){
        return CALENDAR_DATE_FORMAT.format(timestamp.toDate());
    }

    public static Timestamp getEndDateTimestamp(Timestamp startDate, int numDays){
        return new Timestamp(startDate.getSeconds() + TimeUnit.DAYS.toSeconds(numDays), 0);
    }

    public static String getDurationString(Timestamp startDate, Timestamp endDate){
        return String.format("%d days", TimeUnit.MILLISECONDS.toDays(endDate.toDate().getTime() - startDate.toDate().getTime()));
    }

    private static Timestamp convertStringToTimestamp(String dateString, SimpleDateFormat format) {
        try {
            Date date = format.parse(dateString);
            if (date != null) {
                return new Timestamp(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Timestamp convertStringToTimestamp(String dateString) {
        return convertStringToTimestamp(dateString, DEFAULT_DATE_FORMAT);
    }

    public static Timestamp getCalendarTimestamp(String dateString){
        return convertStringToTimestamp(dateString, CALENDAR_DATE_FORMAT);
    }

    public static String convertDateToString(Date date, SimpleDateFormat format){
        return format.format(date);
    }

}
