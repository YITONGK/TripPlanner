package com.example.tripplanner.entity;

import com.example.tripplanner.db.DatabaseInterface;
import com.google.firebase.Timestamp;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Trip {
    private String id;
    private String name;
    // private LocalDate startDate;
    // private LocalDate endDate;
    private Timestamp startDate;
    private Timestamp endDate;
    private int numDays;
    private List<Location> locations;
    private String note;
    private Map<String, List<ActivityItem>> plans;
    private DatabaseInterface database;
    private List<String> userIds;

    // No-argument constructor required for Firestore deserialization
    public Trip() {
        // Initialize fields with default values if necessary
        this.locations = new ArrayList<>();
        this.plans = new HashMap<>();
        this.userIds = new ArrayList<>();
    }

    public Trip(String name, Timestamp startDate, int numDays, List<Location> locations, String userId) {
        this.name = name;
        this.startDate = startDate;
        this.numDays = numDays;
        this.endDate = new Timestamp(startDate.getSeconds() + TimeUnit.DAYS.toSeconds(numDays - 1), 0);
        this.locations = locations;
        this.note = "";
        this.plans = new HashMap<>();
        for (int i = 0; i < numDays; i++) {
            plans.put(String.valueOf(i), new ArrayList<>());
        }
        this.userIds = new ArrayList<>();
        this.userIds.add(userId);
        
    }

    public Trip(String name, Timestamp endDate, Timestamp startDate, List<Location> locations, String userId) {
        this.name = name;
        this.endDate = endDate;
        this.startDate = startDate;
        this.locations = locations;
        this.note = "";
        this.numDays = (int) TimeUnit.SECONDS.toDays(endDate.getSeconds() - startDate.getSeconds());
        this.plans = new HashMap<>();
        for (int i = 0; i < numDays; i++) {
            plans.put(String.valueOf(i), new ArrayList<>());
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
    public Map<String, Object> convertTripToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("numDays", numDays);
        // map.put("locations", locations);
        map.put("locations", locations.stream()
                                          .map(Location::convertLocationToMap)
                                          .collect(Collectors.toList()));
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

    public List<Location> getLocations() {
        return locations;
    }

    public Map<String, List<ActivityItem>> getPlans() {
        return plans;
    }

    public String getNote() {
        return note;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Method to add a location to the trip
    public void addLocation(Location location) {
        this.locations.add(location);
    }

     public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void setNumDays(int numDays) {
        this.numDays = numDays;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLastingDays() {
        long daysBetween = TimeUnit.SECONDS.toDays(endDate.getSeconds() - startDate.getSeconds());
        System.out.println("Days between: " + daysBetween);
        return (int) daysBetween;
    }

    public void setPlans(Map<String, List<ActivityItem>> plans) {
        this.plans = plans;
    }
}

