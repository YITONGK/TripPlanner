package com.example.tripplanner;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreDB {

    private FirebaseFirestore firestore;

    public FirestoreDB() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void createTrip(String userId, Map<String, Object> tripData) {
        firestore.collection("users").document(userId)
                .collection("trips").add(tripData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Debug", "Trip added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.d("Debug", "Error adding trip: " + e.getMessage());
                });
    }

    public void addLocationToTrip(String userId, String tripId, Map<String, Object> locationData) {
        firestore.collection("users").document(userId)
                .collection("trips").document(tripId)
                .collection("locations").add(locationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Debug", "Location added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.d("Debug", "Error adding location: " + e.getMessage());
                });
    }

    // Create Trip in Firestore
    public void createTrip(String userId, Trip trip) {
        Map<String, Object> tripData = convertTripToMap(trip);
        firestore.collection("users").document(userId).collection("trips")
                .add(tripData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Trip added with ID: " + documentReference.getId());
                    // Set the ID back on the trip object
                    trip.setId(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding trip: " + e.getMessage());
                });
    }

    // Convert Trip object to Map for Firestore
    private Map<String, Object> convertTripToMap(Trip trip) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", trip.getName());
        map.put("startDate", trip.getStartDate());
        map.put("endDate", trip.getEndDate());
        return map;
    }

    // Add Location to an existing Trip in Firestore
    public void addLocationToTrip(String userId, String tripId, Location location) {
        Map<String, Object> locationData = convertLocationToMap(location);
        firestore.collection("users").document(userId).collection("trips").document(tripId)
                .collection("locations").add(locationData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Location added with ID: " + documentReference.getId());
                    // Set the ID back on the location object
                    location.setId(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding location: " + e.getMessage());
                });
    }

    // Convert Location object to Map for Firestore
    private Map<String, Object> convertLocationToMap(Location location) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", location.getName());
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        return map;
    }

}
