package com.example.tripplanner.helperclass;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trip {
    private String id;
    private String name;
    private Date startDate;
    private Date endDate;
    private List<Location> locations;

    public Trip(String name, Date startDate, Date endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.locations = new ArrayList<>();
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

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public List<Location> getLocations() {
        return locations;
    }

    // Method to add a location to the trip
    public void addLocation(Location location) {
        this.locations.add(location);
    }
}

