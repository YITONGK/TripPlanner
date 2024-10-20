package com.example.tripplanner.entity;

public class Weather {
    private String locationName;
    private double minTemp;
    private double maxTemp;
    private String description;
    private String icon;

    public Weather(String name, double minTemp, double maxTemp, String description, String icon) {
        this.locationName = name;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.description = description;
        this.icon = icon;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getLocationName() {
        return locationName;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "locationName='" + locationName + '\'' +
                ", minTemp=" + minTemp +
                ", maxTemp=" + maxTemp +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
