package com.example.tripplanner.fragment;

import androidx.lifecycle.ViewModel;

import com.example.tripplanner.entity.ActivityItem;

import java.util.ArrayList;
import java.util.HashMap;

public class PlanViewModel extends ViewModel {
    // For the Trip Note
//    private MutableLiveData<String> tripNote = new MutableLiveData<>();

    // For storing Activity Items per day
    private HashMap<Integer, ArrayList<ActivityItem>> dayActivityMap = new HashMap<>();

//    public MutableLiveData<String> getTripNote() {
//        return tripNote;
//    }

    // Method to get the list of activity items for a specific day
    public ArrayList<ActivityItem> getActivityItemArray(int dayIndex) {
        if (!dayActivityMap.containsKey(dayIndex)) {
            dayActivityMap.put(dayIndex, new ArrayList<>());
        }
        return dayActivityMap.get(dayIndex);
    }
}
