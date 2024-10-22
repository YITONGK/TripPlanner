package com.example.tripplanner.entity;

public class User {
    private String username;
    private String email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // In order to read data into a class
    // each custom class must have a public constructor that takes no arguments
    public User() {
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
