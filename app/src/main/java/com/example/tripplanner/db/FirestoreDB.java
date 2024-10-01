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

    public void getTripByTripId(String tripId, OnSuccessListener<Trip> onSuccessListener, OnFailureListener onFailureListener) {
        firestore.collection("trips")
                .document(tripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            // Parse the fields from the document
                            String name = documentSnapshot.getString("name");
                            Timestamp startDate = documentSnapshot.getTimestamp("startDate");
                            Timestamp endDate = documentSnapshot.getTimestamp("endDate");
                            int numDays = documentSnapshot.contains("numDays") ? documentSnapshot.getLong("numDays").intValue() : 0;
                            String note = documentSnapshot.getString("note");
                            List<String> userIds = (List<String>) documentSnapshot.get("userIds");

                            // Parse locations
                            List<Location> locations = new ArrayList<>();
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) documentSnapshot.get("locations");
                            if (locationsMap != null) {
                                for (Map<String, Object> locMap : locationsMap) {
                                    Location location = new Location(
                                            (String) locMap.get("name"),
                                            ((Number) locMap.get("latitude")).doubleValue(),
                                            ((Number) locMap.get("longitude")).doubleValue()
                                    );
                                    locations.add(location);
                                }
                            }

                            // Parse plans
                            Map<String, Object> plansMap = (Map<String, Object>) documentSnapshot.get("plans");
                            Map<String, List<ActivityItem>> plans = new HashMap<>();
                            if (plansMap != null) {
                                for (Map.Entry<String, Object> entry : plansMap.entrySet()) {
                                    String day = entry.getKey();
                                    List<Map<String, Object>> activityItemsMap = (List<Map<String, Object>>) entry.getValue();
                                    List<ActivityItem> activityItems = new ArrayList<>();
                                    for (Map<String, Object> itemMap : activityItemsMap) {
                                        ActivityItem item = new ActivityItem(
                                                (String) itemMap.get("name"),
                                                (Timestamp) itemMap.get("startTime"),
                                                (Timestamp) itemMap.get("endTime"),
                                                (String) itemMap.get("notes")
                                        );
                                        activityItems.add(item);
                                    }
                                    plans.put(day, activityItems);
                                }
                            }

                            // Create the Trip object
                            Trip trip = new Trip(name, startDate, endDate, locations, userIds != null && !userIds.isEmpty() ? userIds.get(0) : null);
                            trip.setId(documentSnapshot.getId());
                            trip.setNote(note);
                            trip.setPlans(plans);
                            trip.setNumDays(numDays);
                            trip.setUserIds(userIds);

                            onSuccessListener.onSuccess(trip);
                        } catch (Exception e) {
                            Log.e("PLAN", "Error parsing trip document", e);
                            onFailureListener.onFailure(e);
                        }
                    } else {
                        onFailureListener.onFailure(new Exception("Trip not found with ID: " + tripId));
                    }
                })
                .addOnFailureListener(onFailureListener);
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
