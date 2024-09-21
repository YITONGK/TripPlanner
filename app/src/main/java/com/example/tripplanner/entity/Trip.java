package com.example.tripplanner.entity;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trip {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Location> locations;
    private String note;
    private Map<Integer, List<ActivityItem>> plans;

    public Trip(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.locations = new ArrayList<>();
        this.plans = new HashMap<>();
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public Map<Integer, List<ActivityItem>> getPlans() {
        return plans;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStartDate(String startDate){
        this.startDate = LocalDate.parse(startDate);
    }

    public void setEndDate(String endDate){
        this.endDate = LocalDate.parse(endDate);
    }

    // Method to add a location to the trip
    public void addLocation(Location location) {
        this.locations.add(location);
    }

    public int getLastingDays(){
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        System.out.println("Days between: " + daysBetween);
        return (int) daysBetween;
    }
}

