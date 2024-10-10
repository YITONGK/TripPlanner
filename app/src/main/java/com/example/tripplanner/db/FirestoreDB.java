package com.example.tripplanner.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.entity.UserTripStatistics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class FirestoreDB {

    private FirebaseFirestore firestore;

    public FirestoreDB() {
        this.firestore = FirebaseFirestore.getInstance();
    }

     public void getTripsByUserId(String userId, OnSuccessListener<List<Trip>> onSuccessListener, OnFailureListener onFailureListener) {
        Timestamp now = Timestamp.now();
        firestore.collection("trips")
                .whereArrayContains("userIds", userId)
                .whereGreaterThan("startDate", now)
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

    public void getPastTripsByUserId(String userId, OnSuccessListener<List<Trip>> onSuccessListener, OnFailureListener onFailureListener) {
        Timestamp now = Timestamp.now();
        firestore.collection("trips")
                .whereArrayContains("userIds", userId)
                .whereLessThan("startDate", now)
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

    public void getUserTripStatistics(String userId, OnSuccessListener<UserTripStatistics> onSuccessListener, OnFailureListener onFailureListener) {
        Timestamp now = Timestamp.now();
        firestore.collection("trips")
                .whereArrayContains("userIds", userId)
                .whereLessThan("startDate", now)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTrips = 0;
                    int totalDays = 0;
                    Set<String> uniqueLocations = new HashSet<>();

                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    totalTrips = documents.size();

                    for (DocumentSnapshot document : documents) {
                        try {
                            // Parse numDays
                            int numDays = document.contains("numDays") ? document.getLong("numDays").intValue() : 0;
                            totalDays += numDays;

                            // Parse locations
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) document.get("locations");
                            if (locationsMap != null) {
                                for (Map<String, Object> locMap : locationsMap) {
                                    String locationName = (String) locMap.get("name");
                                    double latitude = ((Number) locMap.get("latitude")).doubleValue();
                                    double longitude = ((Number) locMap.get("longitude")).doubleValue();
                                    String locationId = locationName + "_" + latitude + "_" + longitude;

                                    uniqueLocations.add(locationId);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("PLAN", "Error parsing trip document", e);
                            // Continue to next trip
                        }
                    }

                    int totalUniqueLocations = uniqueLocations.size();

                    UserTripStatistics statistics = new UserTripStatistics(totalTrips, totalUniqueLocations, totalDays);
                    onSuccessListener.onSuccess(statistics);

                })
                .addOnFailureListener(onFailureListener);
    }


    public void deleteTripById(String tripId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        firestore.collection("trips")
                .document(tripId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("PLAN", "Trip with ID: " + tripId + " has been successfully deleted.");
                    if (onSuccessListener != null) {
                        onSuccessListener.onSuccess(aVoid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PLAN", "Error deleting trip with ID: " + tripId, e);
                    if (onFailureListener != null) {
                        onFailureListener.onFailure(e);
                    }
                });
    }


}
