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
        this.notes = "";
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

    public String getStartTimeString() {
        if (startTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime.toDate());
            return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
        return "__:__";
    }

    public String getEndTimeString() {
        if (endTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime.toDate());
            return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
        return "__:__";
    }

    public String getLocationString() {
        if (location != null) {
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

    @Override
    public String toString() {
        return "ActivityItem{" +
                "name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", location=" + location +
                ", notes='" + notes + '\'' +
                '}';
    }
}
