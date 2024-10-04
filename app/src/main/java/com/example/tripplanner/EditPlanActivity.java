package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.tripplanner.databinding.ActivityEditPlanBinding;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.PlanFragment;
import com.google.android.material.tabs.TabLayout;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditPlanActivity extends AppCompatActivity {

    private String selectedPlace;
    private int days;
    private String tripName;
    private ActivityEditPlanBinding binding;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private FragmentManager fragmentManager;

    private JSONObject tripPlan;
    private JSONArray placeArray;
    ArrayList<String> placeList = new ArrayList<>();
    private ActivityResultLauncher<Intent> planSettingsLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize variables from intent
        // String jsonString = getIntent().getStringExtra("planDetails");

        // Get plan details from database
        String tripId = getIntent().getStringExtra("tripId");
        Log.d("TAG", tripId);
        FirestoreDB firestoreDB = new FirestoreDB();
        firestoreDB.getTripByTripId(tripId, trip -> {
            Log.d("DDDD", trip.toString());
            extractDetails(trip);
            this.days = trip.getNumDays();
        }, e -> {
            Log.d("PLAN", "Error getting trip by trip id: " + e.getMessage());
        });


//        if (jsonString != null) {
//            try {
//                tripPlan = new JSONObject(jsonString);
//                placeArray = tripPlan.getJSONArray("location");
//                for (int i = 0; i < placeArray.length(); i++) {
//                    placeList.add(placeArray.getString(i));
//                }
//                StringBuilder sb = new StringBuilder();
//                for (String place : placeList) {
//                    sb.append(place).append(", ");
//                }
//                if (sb.length() > 0) {
//                    sb.setLength(sb.length() - 2);
//                }
//                selectedPlace = sb.toString();
//                days = tripPlan.getInt("days");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        // Set trip name and days
        TextView tripTo = findViewById(R.id.textViewSelectedPlace);
        tripName = days + (days > 1 ? " days" : " day") + " trip to " + selectedPlace;
        tripTo.setText(tripName);
        updateDayAndNightText();

        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view -> {
            Intent intent = new Intent(EditPlanActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        planSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra("tripName")) {
                            String newTripName = data.getStringExtra("tripName");
                            TextView tripTo1 = findViewById(R.id.textViewSelectedPlace);
                            tripTo1.setText(newTripName);
                            int newDays = data.getIntExtra("days", 0);
                            if (days != newDays) {
                                if (days < newDays) {
                                    addTabsAndFragments(days, newDays);
                                } else if (days > newDays) {
                                    removeTabsAndFragments(days, newDays);
                                }
                                days = newDays;
                                updateDayAndNightText();
                                refreshTabsAndFragments();
                                loadFragment(fragments.get(0));
                            }
                        }
                    }
                }
        );

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(EditPlanActivity.this, PlanSettingActivity.class);
            intent.putExtra("tripName", tripName);
            intent.putExtra("days", days);
            planSettingsLauncher.launch(intent);
        });

        fragmentManager = getSupportFragmentManager();
        // Initialize fragments and tabs
        initializeFragmentsAndTabs();

        // Set up TabSelectedListener
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = fragments.get(tab.getPosition());
                loadFragment(selectedFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle unselected tab
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle reselected tab
            }
        });
    }

    private void extractDetails(Trip trip) {
        // Get locations and duration of the plan
        List<Location> locations = trip.getLocations();

        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append(location.getName()).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        this.selectedPlace = sb.toString();
        this.days = trip.getNumDays();

        //TODO: get activities of the plan
    }

    private void initializeFragmentsAndTabs() {
        TabLayout tabLayout = binding.tabLayout;
        fragments.clear();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Add Overview Fragment
        tabLayout.addTab(tabLayout.newTab().setText("Overview"));
        Fragment overviewFragment = PlanFragment.newInstance(PlanFragment.OVERVIEW, -1);
        transaction.add(R.id.fragmentContainerView, overviewFragment, "fragment_overview");
        fragments.add(overviewFragment);

        // Add Day Fragments
        for (int i = 0; i < days; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("Day " + (i + 1)));
            Fragment dayFragment = PlanFragment.newInstance(PlanFragment.PLAN_SPECIFIC_DAY, i);
            transaction.add(R.id.fragmentContainerView, dayFragment, "fragment_day_" + i);
            transaction.hide(dayFragment);
            fragments.add(dayFragment);
        }

        transaction.commitNow();
        loadFragment(fragments.get(0));
    }

    private void addTabsAndFragments(int oldDays, int newDays) {
        TabLayout tabLayout = binding.tabLayout;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = oldDays; i < newDays; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("Day " + (i + 1)));
            Fragment dayFragment = PlanFragment.newInstance(PlanFragment.PLAN_SPECIFIC_DAY, i);
            transaction.add(R.id.fragmentContainerView, dayFragment, "fragment_day_" + i);
            transaction.hide(dayFragment);
            fragments.add(dayFragment);
        }
        transaction.commitNow();
    }

    private void removeTabsAndFragments(int oldDays, int newDays) {
        TabLayout tabLayout = binding.tabLayout;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = oldDays - 1; i >= newDays; i--) {
            tabLayout.removeTabAt(i + 1);
            Fragment fragment = fragments.get(i + 1);
            transaction.remove(fragment);
            fragments.remove(i + 1);
        }
        transaction.commitNow();
    }

    private void refreshTabsAndFragments() {
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.removeAllTabs();
        for (int i = 0; i < fragments.size(); i++) {
            if (i == 0) {
                tabLayout.addTab(tabLayout.newTab().setText("Overview"));
            } else {
                tabLayout.addTab(tabLayout.newTab().setText("Day " + i));
            }
        }
    }

    private void updateDayAndNightText() {
        String dayAndNight;
        if (days == 1) {
            dayAndNight = "1 day";
        } else if (days == 2) {
            dayAndNight = "2 days and 1 night";
        } else {
            dayAndNight = days + " days and " + (days - 1) + " nights";
        }
        TextView daysAndNight = findViewById(R.id.textViewDaysAndNights);
        daysAndNight.setText(dayAndNight);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }
}
