package com.example.tripplanner.entity;

public class DistanceMatrixEntry {
    private String from;
    private String to;
    private String mode;
    private String distance;
    private String duration;

    public DistanceMatrixEntry(String from, String to, String mode, String distance, String duration) {
        this.from = from;
        this.to = to;
        this.mode = mode;
        this.distance = distance;
        this.duration = duration;
    }

    // Getters and setters
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getMode() { return mode; }
    public String getDistance() { return distance; }
    public String getDuration() { return duration; }
}