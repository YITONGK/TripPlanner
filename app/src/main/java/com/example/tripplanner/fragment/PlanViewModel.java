package com.example.tripplanner.fragment;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Trip;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanViewModel extends ViewModel {
    private Trip trip;
    private Map<Integer, ArrayList<ActivityItem>> activitiesPerDay;

    public PlanViewModel() {
        activitiesPerDay = new HashMap<>();
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
        // Initialize activitiesPerDay from trip's plans
        Map<String, List<ActivityItem>> tripPlans = trip.getPlans();
        if (tripPlans != null) {
            for (Map.Entry<String, List<ActivityItem>> entry : tripPlans.entrySet()) {
                int dayIndex = Integer.parseInt(entry.getKey());
                activitiesPerDay.put(dayIndex, new ArrayList<>(entry.getValue()));
            }
        }
    }

    public Trip getTrip() {
        return trip;
    }

    public ArrayList<ActivityItem> getActivityItemArray(int dayIndex) {
        if (!activitiesPerDay.containsKey(dayIndex)) {
            activitiesPerDay.put(dayIndex, new ArrayList<>());
        }
        return activitiesPerDay.get(dayIndex);
    }

    public void addActivity(int dayIndex, ActivityItem activityItem) {
        getActivityItemArray(dayIndex).add(activityItem);
        updateTripPlans();
    }

    public void updateActivity(int dayIndex, int position, ActivityItem activityItem) {
        getActivityItemArray(dayIndex).set(position, activityItem);
        updateTripPlans();
    }

    public void removeActivity(int dayIndex, int position) {
        getActivityItemArray(dayIndex).remove(position);
        updateTripPlans();
    }

    private void updateTripPlans() {
        // Convert activitiesPerDay to trip's plans
        Map<String, List<ActivityItem>> tripPlans = new HashMap<>();
        for (Map.Entry<Integer, ArrayList<ActivityItem>> entry : activitiesPerDay.entrySet()) {
            tripPlans.put(String.valueOf(entry.getKey()), new ArrayList<>(entry.getValue()));
        }
        trip.setPlans(tripPlans);
    }

    public void saveTripToDatabase() {
        if (trip != null) {
            FirestoreDB firestoreDB = new FirestoreDB();
            Log.d("trip saved", trip.toString());
            firestoreDB.updateTrip(trip.getId(), trip, success -> {
                if (success) {
                    Log.d("trip saved", "saveTripToDatabase: success");
                } else {
                    Log.e("trip saved", "saveTripToDatabase: fail");
                }
            });
        }
    }
}
