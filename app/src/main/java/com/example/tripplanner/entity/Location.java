package com.example.tripplanner.entity;

import java.util.HashMap;
import java.util.Map;

public class Location {
    private String id;
    private String name;
    private double latitude;
    private double longitude;

    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Map<String, Object> convertLocationToMap() {
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("name", name);
        locationMap.put("latitude", latitude);
        locationMap.put("longitude", longitude);
        return locationMap;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

