package com.example.tripplanner.helperclass;

public class ActivityItem {
    private String name;
    private String time;
    private String location;
    private String notes;

    public ActivityItem (String name) {
        this.name = name;
        this.time = "";
        this.location = "";
        this.notes = "";
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
