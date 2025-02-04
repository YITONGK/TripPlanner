package com.example.tripplanner.entity;

import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class  Location implements Serializable {
    private String id;
    private String name;
    private String type;
    private double latitude;
    private double longitude;
    private String country;

    public Location(String id, String name, String type, double latitude, double longitude, String country) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }

    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location() {}

    public Map<String, Object> convertLocationToMap() {
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("id", id);
        locationMap.put("name", name);
        locationMap.put("type", type);
        locationMap.put("latitude", latitude);
        locationMap.put("longitude", longitude);
        locationMap.put("country", country);
        return locationMap;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getNonNullIdOrName() {
        return id != null ? id : name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("country", country);
        return map;
    }


    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", country='" + country + '\'' +
                '}';
    }

    public static Location getInstanceFromPlace(Place place){
        String country = null;

        // Extract the country from the address components
        if (place.getAddressComponents() != null) {
            for (AddressComponent component : place.getAddressComponents().asList()) {
                if (component.getTypes().contains("country")) {
                    country = component.getName();
                    break;
                }
            }
        }

        // Get the primary place type
        String placeType = null;
        if (place.getTypes() != null && !place.getTypes().isEmpty()) {
            placeType = place.getTypes().get(0).toString();
        }

        // Create a new Location object with the fetched details
        Location location = new Location(
                place.getId(),
                place.getName(),
                placeType,
                place.getLatLng().latitude,
                place.getLatLng().longitude,
                country
        );

        return location;
    }

}

