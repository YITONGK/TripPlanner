package com.example.tripplanner.entity;

import com.example.tripplanner.db.DatabaseInterface;

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
    private int numDays;
    private List<Location> locations;
    private String note;
    private Map<Integer, List<ActivityItem>> plans;
    private DatabaseInterface database;
    private List<String> userIds;

    public Trip(String name, LocalDate startDate, int numDays, List<Location> locations, String userId) {
        this.name = name;
        this.startDate = startDate;
        this.numDays = numDays;
        this.locations = locations;
        this.note = "";
        this.plans = new HashMap<>();
        for (int i = 0; i < numDays; i++) {
            plans.put(i, new ArrayList<>());
        }
        this.userIds = new ArrayList<>();
        this.userIds.add(userId);
        
    }

    public Trip(String name, LocalDate endDate, LocalDate startDate, List<Location> locations, String userId) {
        this.name = name;
        this.endDate = endDate;
        this.startDate = startDate;
        this.locations = locations;
        this.note = "";
        this.numDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
        this.plans = new HashMap<>();
        for (int i = 0; i < numDays; i++) {
            plans.put(i, new ArrayList<>());
        }
        this.userIds = new ArrayList<>();
        this.userIds.add(userId);
    }

    public void uploadTrip() {
        Map<String, Object> tripData = convertTripToMap();
        database.insert("trips", tripData); 
        System.out.println("Uploading trip to database...");

    }

    // Convert Trip object to Map for Firestore
    private Map<String, Object> convertTripToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("numDays", numDays);
        map.put("locations", locations);
        map.put("note", note);
        map.put("plans", plans);
        map.put("userIds", userIds);
        return map;
    }

    public void setId(String id) {
        this.id = id;
    }

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

