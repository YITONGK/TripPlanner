package com.example.tripplanner;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class PlanViewModel extends ViewModel {
    private MutableLiveData<String> tripNote = new MutableLiveData<>();

    private HashMap<Integer, ArrayList<ActivityItem>> dayActivityMap = new HashMap<>();

    public MutableLiveData<String> getTripNote() {
        return tripNote;
    }

    public ArrayList<ActivityItem> getActivityItemArray(int dayIndex) {
        if (!dayActivityMap.containsKey(dayIndex)) {
            dayActivityMap.put(dayIndex, new ArrayList<>());
        }
        return dayActivityMap.get(dayIndex);
    }
}

