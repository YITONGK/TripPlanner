package com.example.tripplanner.entity;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String username;
    private String email;
    private String profileImagePath; // URI or path to the profile image
    private String preference;

    // Used to create a new user
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.preference = "Enjoys coffee and outdoor activities";
    }

    // In order to read data into a class
    public User(String id, String username, String email, String preference) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.preference = preference;
    }

    // In order to read data into a class
    // each custom class must have a public constructor that takes no arguments
    public User() {
    }

    public Map<String, Object> convertUserToMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("email", email);
        map.put("preference", preference);
        return map;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
