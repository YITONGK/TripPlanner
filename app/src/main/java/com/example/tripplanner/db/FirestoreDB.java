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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class FirestoreDB {

    private FirebaseFirestore firestore;
    private static FirestoreDB instance; // Singleton instance

    // Private constructor to prevent instantiation
    private FirestoreDB() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public final static String getCurrentUserId() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        return currentUserId;
    }

    // Public method to provide access to the singleton instance
    public static synchronized FirestoreDB getInstance() {
        if (instance == null) {
            instance = new FirestoreDB();
        }
        return instance;
    }

    private Timestamp getCurrentDate() {
        // Define the timezone
        ZoneId zoneId = ZoneId.of("UTC+11");

        // Get the current date and time in the specified timezone
        LocalDateTime localDateTime = LocalDateTime.now(zoneId);

        // Set the time to the start of the day
        LocalDateTime startOfDay = localDateTime.toLocalDate().atStartOfDay();

        // Convert to ZonedDateTime
        ZonedDateTime zonedStartOfDay = startOfDay.atZone(zoneId);

        // Convert to Date
        Date date = Date.from(zonedStartOfDay.toInstant());

        // Return as Timestamp
        return new Timestamp(date);
    }

    public void getTripsByUserId(String userId, OnSuccessListener<List<Trip>> onSuccessListener,
            OnFailureListener onFailureListener) {
        Timestamp now = getCurrentDate();
        firestore.collection("trips")
                .whereArrayContains("userIds", userId)
                .whereGreaterThanOrEqualTo("startDate", now)
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
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) document
                                    .get("locations");
                            List<Location> locations = new ArrayList<>();
                            for (Map<String, Object> locMap : locationsMap) {
                                Location location = new Location(
                                        (String) locMap.get("id"),
                                        (String) locMap.get("name"),
                                        (String) locMap.get("type"),
                                        ((Number) locMap.get("latitude")).doubleValue(),
                                        ((Number) locMap.get("longitude")).doubleValue(),
                                        (String) locMap.get("country"));
                                locations.add(location);
                            }
                            String note = document.getString("note");
                            Map<String, List<ActivityItem>> plans = (Map<String, List<ActivityItem>>) document
                                    .get("plans");
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

    public void getPastTripsByUserId(String userId, OnSuccessListener<List<Trip>> onSuccessListener,
            OnFailureListener onFailureListener) {
        Timestamp now = getCurrentDate();
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
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) document
                                    .get("locations");
                            List<Location> locations = new ArrayList<>();
                            for (Map<String, Object> locMap : locationsMap) {
                                Location location = new Location(
                                        document.getId(),
                                        (String) locMap.get("name"),
                                        (String) locMap.get("type"),
                                        ((Number) locMap.get("latitude")).doubleValue(),
                                        ((Number) locMap.get("longitude")).doubleValue(),
                                        (String) locMap.get("country"));
                                locations.add(location);
                            }
                            String note = document.getString("note");
                            Map<String, List<ActivityItem>> plans = (Map<String, List<ActivityItem>>) document
                                    .get("plans");
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
                });
        ;
    }

    public void createTrip(String userId, Trip trip, OnSuccessListener<Trip> onSuccessListener,
            OnFailureListener onFailureListener) {
        Map<String, Object> tripData = trip.convertTripToMap();
        firestore.collection("trips").add(tripData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("PLAN", "DocumentSnapshot written with ID: " + documentReference.getId());
                        // Update the original Trip object with the new ID
                        trip.setId(documentReference.getId());

                        // Trigger the success listener with the updated Trip object
                        onSuccessListener.onSuccess(trip);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("PLAN", "Error adding document", e);
                    }
                });
        ;
    }

    public void getTripByTripId(String tripId, OnSuccessListener<Trip> onSuccessListener,
            OnFailureListener onFailureListener) {
        if (tripId == null || tripId.isEmpty()) {
            Log.e("FirestoreDB", "Invalid trip ID: " + tripId);
            if (onFailureListener != null) {
                onFailureListener.onFailure(new IllegalArgumentException("Trip ID cannot be null or empty"));
            }
            return;
        }
        Log.d("FirestoreDB", tripId);
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
                            int numDays = documentSnapshot.contains("numDays")
                                    ? documentSnapshot.getLong("numDays").intValue()
                                    : 0;
                            String note = documentSnapshot.getString("note");
                            List<String> userIds = (List<String>) documentSnapshot.get("userIds");

                            // Parse locations
                            List<Location> locations = new ArrayList<>();
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) documentSnapshot
                                    .get("locations");
                            if (locationsMap != null) {
                                for (Map<String, Object> locMap : locationsMap) {
                                    Location location = new Location(
                                            (String) locMap.get("id"),
                                            (String) locMap.get("name"),
                                            (String) locMap.get("type"),
                                            ((Number) locMap.get("latitude")).doubleValue(),
                                            ((Number) locMap.get("longitude")).doubleValue(),
                                            (String) locMap.get("country"));
                                    locations.add(location);
                                }
                            }

                            // Parse plans
                            Map<String, Object> plansMap = (Map<String, Object>) documentSnapshot.get("plans");
                            Map<String, List<ActivityItem>> plans = new HashMap<>();
                            if (plansMap != null) {
                                for (Map.Entry<String, Object> entry : plansMap.entrySet()) {
                                    String day = entry.getKey();
                                    List<Map<String, Object>> activityItemsMap = (List<Map<String, Object>>) entry
                                            .getValue();
                                    List<ActivityItem> activityItems = new ArrayList<>();
                                    for (Map<String, Object> itemMap : activityItemsMap) {
                                        ActivityItem item = new ActivityItem();
                                        item.setName((String) itemMap.get("name"));
                                        item.setStartTime((Timestamp) itemMap.get("startTime"));
                                        item.setEndTime((Timestamp) itemMap.get("endTime"));
                                        item.setNotes((String) itemMap.get("notes"));

                                        // Reconstruct Location
                                        Map<String, Object> locationMap = (Map<String, Object>) itemMap.get("location");
                                        if (locationMap != null) {
                                            Location location = new Location();
                                            location.setId((String) locationMap.get("id"));
                                            location.setName((String) locationMap.get("name"));
                                            location.setType((String) locationMap.get("type"));
                                            location.setLatitude(((Number) locationMap.get("latitude")).doubleValue());
                                            location.setLongitude(
                                                    ((Number) locationMap.get("longitude")).doubleValue());
                                            item.setLocation(location);
                                        }
                                        activityItems.add(item);
                                    }
                                    plans.put(day, activityItems);
                                }
                            }

                            // Create the Trip object
                            Trip trip = new Trip(name, startDate, endDate, locations,
                                    userIds != null && !userIds.isEmpty() ? userIds.get(0) : null);
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

    public void getUserTripStatistics(String userId, OnSuccessListener<UserTripStatistics> onSuccessListener,
            OnFailureListener onFailureListener) {
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
                            List<Map<String, Object>> locationsMap = (List<Map<String, Object>>) document
                                    .get("locations");
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

    public void deleteTripById(String tripId, OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener) {
        String userId = getCurrentUserId();
        DocumentReference tripRef = firestore.collection("trips").document(tripId);

        tripRef.update("userIds", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    // Check if the userIds array is empty
                    tripRef.get().addOnSuccessListener(documentSnapshot -> {
                        List<String> userIds = (List<String>) documentSnapshot.get("userIds");
                        if (userIds == null || userIds.isEmpty()) {
                            // Delete the trip if no users are left
                            tripRef.delete().addOnSuccessListener(aVoid2 -> {
                                Log.d("PLAN", "Trip with ID: " + tripId + " has been successfully deleted.");
                                if (onSuccessListener != null) {
                                    onSuccessListener.onSuccess(aVoid2);
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("PLAN", "Error deleting trip with ID: " + tripId, e);
                                if (onFailureListener != null) {
                                    onFailureListener.onFailure(e);
                                }
                            });
                        } else {
                            Log.d("PLAN", "User " + userId + " removed from trip with ID: " + tripId);
                            if (onSuccessListener != null) {
                                onSuccessListener.onSuccess(aVoid);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("PLAN", "Error fetching trip with ID: " + tripId, e);
                        if (onFailureListener != null) {
                            onFailureListener.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("PLAN", "Error removing user from trip with ID: " + tripId, e);
                    if (onFailureListener != null) {
                        onFailureListener.onFailure(e);
                    }
                });
    }

    // Add new user into an existing trip
    public void addUserToTrip(String tripId, String newUserId, OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener) {
        DocumentReference tripRef = firestore.collection("trips").document(tripId);

        tripRef.update("userIds", FieldValue.arrayUnion(newUserId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("PLAN", "User " + newUserId + " added to trip with ID: " + tripId);
                    if (onSuccessListener != null) {
                        onSuccessListener.onSuccess(aVoid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PLAN", "Error adding user to trip with ID: " + tripId, e);
                    if (onFailureListener != null) {
                        onFailureListener.onFailure(e);
                    }
                });
    }

    public void updateTrip(String tripId, Trip trip, OnSuccessListener<Boolean> listener) {
        firestore.collection("trips").document(tripId)
                .set(trip.convertTripToMap())
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    listener.onSuccess(false);
                });
    }

}
