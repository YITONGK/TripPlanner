package com.example.tripplanner.helperclass;

public class ActivityItem {
    private String name;
    private String startTime;
    private String endTime;
    private String location;
    private String notes;

    public ActivityItem (String name) {
        this.name = name;
        this.startTime = "";
        this.endTime = "";
        this.location = "";
        this.notes = "";
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String time) {
        this.startTime = time;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String time) {
        this.endTime = time;
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
