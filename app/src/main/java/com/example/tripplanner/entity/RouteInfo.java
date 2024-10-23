package com.example.tripplanner.entity;

public class RouteInfo {
    private String duration;
    private String distance;

    public RouteInfo(String duration, String distance) {
        this.duration = duration;
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public String getDistance() {
        return distance;
    }
}
