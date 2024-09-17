// PlanViewModel.java
package com.example.tripplanner;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlanViewModel extends ViewModel {
    private MutableLiveData<String> tripNote = new MutableLiveData<>();

    public MutableLiveData<String> getTripNote() {
        return tripNote;
    }
}
