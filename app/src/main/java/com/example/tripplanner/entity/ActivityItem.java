package com.example.tripplanner.entity;

public class ActivityItem {
    private String name;
    private String startTime;
    private String endTime;
    private String location;
    private String notes;

    public ActivityItem (String name) {
        this.name = name;
        this.startTime = "";
        this.location = "";
        this.notes = "";
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return startTime;
    }

    public void setTime(String time) {
        this.startTime = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
