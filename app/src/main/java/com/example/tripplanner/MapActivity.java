package com.example.tripplanner;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripplanner.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.gms.maps.model.LatLngBounds;



public class MapActivity extends AppCompatActivity {
    private GoogleMap googleMap;
    private ActivityMapBinding binding;
    private HashMap<String, List<Double[]>> receivedMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpCloseButton();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_overview);

        if (mapFragment != null) {
            mapFragment.getMapAsync(gMap -> {
                googleMap = gMap;
                addMarkersToMap(0);
            });
        }

        initializeFragmentsAndTabs();
    }

    private void addMarkersToMap(int tabPosition) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        if (receivedMap != null) {
            if (tabPosition == 0) {
                for (String key : receivedMap.keySet()) {
                    List<Double[]> latLngList = receivedMap.get(key);
                    addMarkersForLatLngList(latLngList, key, boundsBuilder);
                }
            } else {
                String key = String.valueOf(tabPosition - 1);
                List<Double[]> latLngList = receivedMap.get(key);
                if (latLngList == null || latLngList.isEmpty()) {
                    googleMap.clear();
                    return;
                }
                addMarkersForLatLngList(latLngList, "Key " + key, boundsBuilder);
            }
            int padding = 100;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
        }
    }

    private void addMarkersForLatLngList(List<Double[]> latLngList, String title, LatLngBounds.Builder boundsBuilder) {
        for (Double[] latLng : latLngList) {
            double latitude = latLng[0];
            double longitude = latLng[1];
            LatLng location = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(location).title(title));
            boundsBuilder.include(location);
        }
    }

    private void setUpCloseButton(){
        ImageView backButton = findViewById(R.id.imageView);
        backButton.setClickable(true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeFragmentsAndTabs() {
        Intent intent = getIntent();
        receivedMap = (HashMap<String, List<Double[]>>) intent.getSerializableExtra("daysAndLocationsMap", HashMap.class);

        if (receivedMap == null) {
            Log.e("MapActivity", "receivedMap is null!");
        } else {
            Log.d("MapActivity", "receivedMap has been received.");
        }

        int numDays = getIntent().getIntExtra("numDays", 0);
        TabLayout tabLayout = binding.tabLayoutOverview;

        tabLayout.removeAllTabs();

        tabLayout.addTab(tabLayout.newTab().setText("OVERVIEW"));

        for (int i = 0; i < numDays; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("DAY" + (i + 1)));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                addMarkersToMap(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


}
