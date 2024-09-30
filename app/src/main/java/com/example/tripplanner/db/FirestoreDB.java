package com.example.tripplanner.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class FirestoreDB implements DatabaseInterface {

    private FirebaseFirestore firestore;

    public FirestoreDB() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public String insert(String table, Map<String, Object> values) {
        AtomicReference<String> newID = new AtomicReference<>("");
        firestore.collection(table).add(values)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Debug", "Document added with ID: " + documentReference.getId());
                    newID.set(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.d("Debug", "Error adding document: " + e.getMessage());
                });
        return newID.get();
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

     public void getTripsByUserId(String userId, OnSuccessListener<List<Trip>> onSuccessListener, OnFailureListener onFailureListener) {
        firestore.collection("trips")
                .whereArrayContains("userIds", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    
                    List<Trip> trips = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Log.d("PLAN", String.valueOf(document));
                        // Trip trip = document.toObject(Trip.class);
                        // trips.add(trip);
                        try {
                            // Manually parse the fields
                            String name = document.getString("name");
                            Timestamp startDate = document.getTimestamp("startDate");
                            Timestamp endDate = document.getTimestamp("endDate");
                            int numDays = document.getLong("numDays").intValue();
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) document.get("locations");
                            List<Location> locations = new ArrayList<>();
                            for (Map<String, Object> locMap : locationsMap) {
                                Location location = new Location(
                                        (String) locMap.get("name"),
                                        ((Number) locMap.get("latitude")).doubleValue(),
                                        ((Number) locMap.get("longitude")).doubleValue()
                                );
                                locations.add(location);
                            }
                            String note = document.getString("note");
                            Map<String, List<ActivityItem>> plans = (Map<String, List<ActivityItem>>) document.get("plans");
                            List<String> userIds = (List<String>) document.get("userIds");

                            Trip trip = new Trip(name, startDate, endDate, locations, userIds.get(0));
                            trip.setId(document.getId());
                            trip.setNote(note);
                            trip.setPlans(plans);
                            trips.add(trip);
                        } catch (Exception e) {
                            Log.e("PLAN", "Error parsing trip document", e);
                        }
                    }
                    onSuccessListener.onSuccess(trips);
                })
                .addOnFailureListener(onFailureListener);
    }


    // Create Trip in Firestore
//    public void createTrip(String userId, Trip trip) {
//        Map<String, Object> tripData = convertTripToMap(trip);
//        insert("users/" + userId + "/trips", tripData);
//    }

    // Add Location to an existing Trip in Firestore
    // public void addLocationToTrip(String userId, String tripId, Location location) {
    //     Map<String, Object> locationData = convertLocationToMap(location);
    //     insert("users/" + userId + "/trips/" + tripId + "/locations", locationData);
    // }

    public void createTrip(String userId, Map<String, Object> tripData) {
        firestore.collection("trips").add(tripData)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d("PLAN", "DocumentSnapshot written with ID: " + documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("PLAN", "Error adding document", e);
                }
            });;
    }

    // public void addLocationToTrip(String userId, String tripId, Map<String, Object> locationData) {
    //     firestore.collection("users").document(userId)
    //             .collection("trips").document(tripId)
    //             .collection("locations").add(locationData)
    //             .addOnSuccessListener(documentReference -> {
    //                 Log.d("Debug", "Location added with ID: " + documentReference.getId());
    //             })
    //             .addOnFailureListener(e -> {
    //                 Log.d("Debug", "Error adding location: " + e.getMessage());
    //             });
    // }


    // Convert Location object to Map for Firestore
    private Map<String, Object> convertLocationToMap(Location location) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", location.getName());
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        return map;
    }

}
