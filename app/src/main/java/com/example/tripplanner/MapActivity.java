package com.example.tripplanner;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.tripplanner.databinding.ActivityEditPlanBinding;
import com.example.tripplanner.fragment.PlanFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private ActivityEditPlanBinding binding;
    private final ArrayList<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        HashMap<String, List<Double[]>> receivedMap = (HashMap<String, List<Double[]>>) intent.getSerializableExtra("daysAndLocationsMap", HashMap.class);

        if (receivedMap != null) {
            for (String key : receivedMap.keySet()) {
                List<Double[]> latLngList = receivedMap.get(key);
                for (Double[] latLng : latLngList) {
                    double latitude = latLng[0];
                    double longitude = latLng[1];
//                    Log.d("MapActivity", "Key: " + key + ", Latitude: " + latitude + ", Longitude: " + longitude);
                }
            }
        }

    }

}
