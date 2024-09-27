package com.example.tripplanner.entity;

public class Weather {
    private double minTemp;
    private double maxTemp;
    private String description;
    private String icon;

    public Weather(double minTemp, double maxTemp, String description, String icon) {
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
}
