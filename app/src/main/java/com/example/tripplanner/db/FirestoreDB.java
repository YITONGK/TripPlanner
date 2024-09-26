package com.example.tripplanner.db;

import android.util.Log;

import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreDB implements DatabaseInterface {

    private FirebaseFirestore firestore;

    public FirestoreDB() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void insert(String table, Map<String, Object> values) {
        firestore.collection(table).add(values)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Debug", "Document added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.d("Debug", "Error adding document: " + e.getMessage());
                });
    }

    @Override
    public void update(String table, Map<String, Object> values, String whereClause) {
        firestore.collection(table).document(whereClause).set(values)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Debug", "Document updated");
                })
                .addOnFailureListener(e -> {
                    Log.d("Debug", "Error updating document: " + e.getMessage());
                });
    }

    @Override
    public void delete(String table, String whereClause) {
        firestore.collection(table).document(whereClause).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Debug", "Document deleted");
                })
                .addOnFailureListener(e -> {
                    Log.d("Debug", "Error deleting document: " + e.getMessage());
                });
    }

    @Override
    public List<Map<String, Object>> query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        // Implement query logic if needed
        return null;
    }


    // Create Trip in Firestore
    public void createTrip(String userId, Trip trip) {
        Map<String, Object> tripData = convertTripToMap(trip);
        insert("users/" + userId + "/trips", tripData);
    }

    // Add Location to an existing Trip in Firestore
    public void addLocationToTrip(String userId, String tripId, Location location) {
        Map<String, Object> locationData = convertLocationToMap(location);
        insert("users/" + userId + "/trips/" + tripId + "/locations", locationData);
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


    // Convert Location object to Map for Firestore
    private Map<String, Object> convertLocationToMap(Location location) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", location.getName());
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        return map;
    }

}
