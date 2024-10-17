package com.example.tripplanner.fragment;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class PlanViewModel extends ViewModel {
    private MutableLiveData<Trip> tripLiveData = new MutableLiveData<>();
    private Map<Integer, ArrayList<ActivityItem>> activitiesPerDay = new HashMap<>();

    public void setTrip(@NonNull Trip trip) {
        tripLiveData.setValue(trip);
        // Initialize activitiesPerDay from trip's plans
        Map<String, List<ActivityItem>> tripPlans = trip.getPlans();
        if (tripPlans != null) {
            for (Map.Entry<String, List<ActivityItem>> entry : tripPlans.entrySet()) {
                int dayIndex = Integer.parseInt(entry.getKey());
                activitiesPerDay.put(dayIndex, new ArrayList<>(entry.getValue()));
            }
        }
    }

    public LiveData<Trip> getTripLiveData() {
        return tripLiveData;
    }

    public Trip getTrip() {
        return tripLiveData.getValue();
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
        Trip currentTrip = tripLiveData.getValue();
        if (currentTrip != null) {
            currentTrip.setPlans(tripPlans);
        }
    }

    public void saveTripToDatabase() {
        Trip currentTrip = tripLiveData.getValue();
        if (currentTrip != null) {
            FirestoreDB firestoreDB = FirestoreDB.getInstance();
            Log.d("trip saved", currentTrip.toString());
            firestoreDB.updateTrip(currentTrip.getId(), currentTrip, success -> {
                if (success) {
                    Log.d("trip saved", "saveTripToDatabase: success");
                } else {
                    Log.e("trip saved", "saveTripToDatabase: fail");
                }
            });
        }
    }

    public void updateActivityList(int dayIndex, ArrayList<ActivityItem> updatedList) {
        activitiesPerDay.put(dayIndex, updatedList);
        updateTripPlans();
        Trip currentTrip = tripLiveData.getValue();
        if (currentTrip != null) {
            tripLiveData.setValue(currentTrip);
        }
    }

}
