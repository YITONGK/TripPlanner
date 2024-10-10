package com.example.tripplanner.entity;

public class UserTripStatistics {
    private int totalTrips;
    private int totalLocations;
    private int totalDays;

    public UserTripStatistics(int totalTrips, int totalLocations, int totalDays) {
        this.totalTrips = totalTrips;
        this.totalLocations = totalLocations;
        this.totalDays = totalDays;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public int getTotalLocations() {
        return totalLocations;
    }

    public int getTotalDays() {
        return totalDays;
    }
}

