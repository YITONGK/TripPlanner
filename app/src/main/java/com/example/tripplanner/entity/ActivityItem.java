package com.example.tripplanner.entity;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivityItem {
    private String name;
    private Timestamp startTime;
    private Timestamp endTime;
    private Location location;
    private String notes;

    public ActivityItem (String name) {
        this.name = name;
//        this.startTime = Timestamp.now();
//        this.endTime = Timestamp.now();
//        this.location = new Location("", 0, 0);
        this.notes = "";
    }

    public ActivityItem(String name, Timestamp startTime, Timestamp endTime, String notes) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    public ActivityItem () {}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

//    public void setStartTime(String startTime) {
//        this.startTime = convertStringToTimestamp(startTime);
//    }

//    public void setEndTime(String endTime) {
//        this.endTime = convertStringToTimestamp(endTime);
//    }

    public String getStartTimeString() {
        if (startTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime.toDate());
            return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
        return "";
    }

    public String getEndTimeString() {
        if (endTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime.toDate());
            return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
        return "";
    }

    public String getLocationString() {
        if (location != null) {
            Log.d("try to get location name", location.toString());
            return location.getName();
        }
        return "";
    }

    public static Timestamp convertStringToTimestamp(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date date = sdf.parse(time);
            Timestamp timestamp = new Timestamp(date);
            return timestamp;
        } catch (ParseException e) {
            Log.d("PLAN", "[ActivityItem] Invalid StartTime");
        }
        return null;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Location getLocation() {
        if (location != null) {
            return location;
        }
        return null;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

//    public void setLocation(String location) {
//        this.location = new Location("", location, "", 0, 0);
//        Log.d("PLAN", "[ActivityItem] setLocation by String");
//    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("notes", notes);
        if (location != null) {
            map.put("location", location.toMap());
        }
        return map;
    }
}
